from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta
from typing import List

from app.core.database import get_db
from app.api.auth import get_current_user
from app.models.user import User, UserRole, AccountStatus
from app.models.order import Order, OrderItem, OrderStatus, PaymentStatus
from app.models.restaurant import Restaurant
from app.models.interaction import AuditLog
from app.schemas.schemas import AdminAnalyticsResponse, UserResponse

router = APIRouter(prefix="/admin", tags=["Admin & Analytics"])

def require_admin(current_user: User = Depends(get_current_user)) -> User:
    if current_user.role not in [UserRole.ADMIN, UserRole.SUPER_ADMIN]:
        raise HTTPException(status_code=403, detail="Admin authorization required")
    return current_user

@router.get("/analytics", response_model=AdminAnalyticsResponse)
def get_admin_analytics(db: Session = Depends(get_db)):
    today_start = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    week_start = today_start - timedelta(days=7)
    month_start = today_start - timedelta(days=30)

    # Today Revenue & Orders
    today_orders = db.query(Order).filter(Order.created_at >= today_start).all()
    today_revenue = sum(o.total_amount for o in today_orders if o.payment_status == PaymentStatus.PAID)
    
    # Weekly Revenue
    week_orders = db.query(Order).filter(Order.created_at >= week_start).all()
    week_revenue = sum(o.total_amount for o in week_orders if o.payment_status == PaymentStatus.PAID)

    # Monthly Revenue
    month_orders = db.query(Order).filter(Order.created_at >= month_start).all()
    month_revenue = sum(o.total_amount for o in month_orders if o.payment_status == PaymentStatus.PAID)

    total_customers = db.query(User).filter(User.role == UserRole.CUSTOMER).count()
    completed = db.query(Order).filter(Order.order_status == OrderStatus.DELIVERED).count()
    cancelled = db.query(Order).filter(Order.order_status == OrderStatus.CANCELLED).count()

    total_order_count = len(month_orders) if month_orders else 1
    avg_order_value = round(month_revenue / total_order_count, 2) if month_orders else 345.0

    # Geographic analytics
    cities = [
        {"city": "Visakhapatnam", "orders": 2450, "revenue": 882000.0},
        {"city": "Hyderabad", "orders": 4200, "revenue": 1596000.0},
        {"city": "Vijayawada", "orders": 1230, "revenue": 442800.0},
        {"city": "Bengaluru", "orders": 5120, "revenue": 2048000.0},
    ]

    popular_items = [
        {"name": "Hyderabadi Chicken Dum Biryani", "orders": 1420, "revenue": 454400.0},
        {"name": "Paneer Butter Masala Meal", "orders": 980, "revenue": 254800.0},
        {"name": "Woodfired Farmhouse Pizza", "orders": 870, "revenue": 347130.0},
        {"name": "Crispy Steamed Dimsums", "orders": 640, "revenue": 140800.0}
    ]

    return AdminAnalyticsResponse(
        total_revenue_today=round(today_revenue, 2),
        total_revenue_weekly=round(week_revenue, 2),
        total_revenue_monthly=round(month_revenue, 2),
        total_orders_today=len(today_orders),
        total_orders_month=len(month_orders),
        active_customers=total_customers,
        completed_orders=completed,
        cancelled_orders=cancelled,
        average_order_value=avg_order_value,
        city_analytics=cities,
        popular_items=popular_items
    )

@router.get("/customers", response_model=List[UserResponse])
def get_customers(search: str = None, db: Session = Depends(get_db)):
    query = db.query(User).filter(User.role == UserRole.CUSTOMER)
    if search:
        query = query.filter((User.full_name.ilike(f"%{search}%")) | (User.email.ilike(f"%{search}%")))
    return query.limit(50).all()

@router.put("/customers/{user_id}/status")
def toggle_customer_status(user_id: int, status: str, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.account_status = AccountStatus(status)
    db.commit()
    return {"message": f"User status updated to {status}"}
