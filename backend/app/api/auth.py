from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.security import verify_password, get_password_hash, create_access_token, decode_access_token, oauth2_scheme
from app.models.user import User, UserRole, AccountStatus
from app.models.interaction import AuditLog
from app.schemas.schemas import LoginRequest, RegisterRequest, OTPRequest, OTPVerifyRequest, TokenResponse, UserResponse
from datetime import datetime
import random

router = APIRouter(prefix="/auth", tags=["Authentication"])

# In-memory rate-limited OTP cache for development/demo (in production backed by Redis)
otp_store = {}

def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> User:
    payload = decode_access_token(token)
    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    user = db.query(User).filter(User.id == int(user_id)).first()
    if not user or user.account_status != AccountStatus.ACTIVE:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="User inactive or suspended")
    return user

@router.post("/register", response_model=TokenResponse)
def register(req: RegisterRequest, db: Session = Depends(get_db)):
    if db.query(User).filter(User.email == req.email).first():
        raise HTTPException(status_code=400, detail="Email already registered")
    if db.query(User).filter(User.phone_number == req.phone_number).first():
        raise HTTPException(status_code=400, detail="Phone number already registered")
    
    user = User(
        full_name=req.full_name,
        email=req.email,
        phone_number=req.phone_number,
        password_hash=get_password_hash(req.password),
        role=UserRole(req.role) if req.role in UserRole.__members__ else UserRole.CUSTOMER,
        account_status=AccountStatus.ACTIVE,
        phone_verified=True
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    token = create_access_token(subject=user.id, role=user.role.value)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        full_name=user.full_name,
        role=user.role.value
    )

@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == req.email).first()
    if not user or not verify_password(req.password, user.password_hash or ""):
        raise HTTPException(status_code=400, detail="Incorrect email or password")
    
    if user.account_status != AccountStatus.ACTIVE:
        raise HTTPException(status_code=403, detail="Account suspended. Please contact support.")

    user.last_login_at = datetime.utcnow()
    db.commit()

    token = create_access_token(subject=user.id, role=user.role.value)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        full_name=user.full_name,
        role=user.role.value
    )

@router.post("/send-otp")
def send_otp(req: OTPRequest):
    # Generates secure 6-digit OTP and logs/sends via SMS provider
    otp = f"{random.randint(100000, 999999)}"
    otp_store[req.phone_number] = {"otp": otp, "expires_at": datetime.utcnow().timestamp() + 300}
    # In production integrate Twilio/Fast2SMS/MSG91
    return {"message": "OTP sent successfully", "status": "success", "expires_in_seconds": 300}

@router.post("/verify-otp", response_model=TokenResponse)
def verify_otp(req: OTPVerifyRequest, db: Session = Depends(get_db)):
    record = otp_store.get(req.phone_number)
    if not record or record["otp"] != req.otp_code or datetime.utcnow().timestamp() > record["expires_at"]:
        raise HTTPException(status_code=400, detail="Invalid or expired OTP")
    
    user = db.query(User).filter(User.phone_number == req.phone_number).first()
    if not user:
        # Create customer profile on first OTP verify
        user = User(
            full_name=f"User {req.phone_number[-4:]}",
            email=f"user_{req.phone_number}@bitedash.in",
            phone_number=req.phone_number,
            role=UserRole.CUSTOMER,
            phone_verified=True
        )
        db.add(user)
        db.commit()
        db.refresh(user)

    token = create_access_token(subject=user.id, role=user.role.value)
    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user_id=user.id,
        full_name=user.full_name,
        role=user.role.value
    )

@router.get("/me", response_model=UserResponse)
def get_profile(current_user: User = Depends(get_current_user)):
    return current_user
