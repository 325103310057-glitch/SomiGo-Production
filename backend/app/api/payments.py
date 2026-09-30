from fastapi import APIRouter, Depends, HTTPException, Header, Request
from sqlalchemy.orm import Session
import hmac
import hashlib
import uuid
from app.core.database import get_db
from app.core.config import settings
from app.api.auth import get_current_user
from app.models.user import User
from app.models.order import Order, Payment, PaymentStatus, OrderStatus
from app.schemas.schemas import PaymentCreateRequest, PaymentWebhookPayload

router = APIRouter(prefix="/payments", tags=["Payments"])

@router.post("/create")
def create_payment_intent(req: PaymentCreateRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == req.order_id, Order.customer_id == current_user.id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")
    
    # Generate gateway order identifier
    gateway_order_id = f"rzp_order_{uuid.uuid4().hex[:12]}"
    
    payment = Payment(
        order_id=order.id,
        user_id=current_user.id,
        payment_provider="RAZORPAY",
        transaction_id=gateway_order_id,
        amount=order.total_amount,
        currency="INR",
        payment_status=PaymentStatus.PENDING,
        payment_method=req.payment_method
    )
    db.add(payment)
    db.commit()

    return {
        "gateway_order_id": gateway_order_id,
        "amount": order.total_amount,
        "currency": "INR",
        "key_id": settings.RAZORPAY_KEY_ID
    }

@router.post("/webhook")
async def payment_webhook(request: Request, db: Session = Depends(get_db)):
    body = await request.body()
    signature = request.headers.get("X-Razorpay-Signature")

    # Authoritative webhook signature verification
    if signature:
        expected_sig = hmac.new(
            settings.PAYMENT_WEBHOOK_SECRET.encode(),
            body,
            hashlib.sha256
        ).hexdigest()
        if not hmac.compare_digest(signature, expected_sig):
            raise HTTPException(status_code=400, detail="Invalid signature")

    payload = await request.json()
    order_number = payload.get("order_number")
    transaction_id = payload.get("transaction_id")
    event = payload.get("event")

    order = db.query(Order).filter(Order.order_number == order_number).first()
    if order and event == "payment.captured":
        order.payment_status = PaymentStatus.PAID
        payment = db.query(Payment).filter(Payment.order_id == order.id).first()
        if payment:
            payment.payment_status = PaymentStatus.PAID
            payment.transaction_id = transaction_id or payment.transaction_id
        db.commit()

    return {"status": "ok"}
