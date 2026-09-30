from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from app.core.database import Base

class Review(Base):
    __tablename__ = "reviews"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id"), nullable=False, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"), nullable=False, unique=True)
    rating = Column(Float, nullable=False)
    review_text = Column(Text, nullable=True)
    restaurant_reply = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="reviews")
    restaurant = relationship("Restaurant", back_populates="reviews")

class Coupon(Base):
    __tablename__ = "coupons"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(50), unique=True, index=True, nullable=False)
    description = Column(String(255), nullable=False)
    discount_type = Column(String(20), default="PERCENTAGE") # PERCENTAGE, FLAT
    discount_value = Column(Float, nullable=False)
    minimum_order_value = Column(Float, default=149.0)
    maximum_discount = Column(Float, default=100.0)
    usage_limit = Column(Integer, default=1000)
    per_user_limit = Column(Integer, default=3)
    valid_from = Column(DateTime, default=datetime.utcnow)
    valid_until = Column(DateTime, nullable=True)
    is_active = Column(Boolean, default=True)

class Favorite(Base):
    __tablename__ = "favorites"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id", ondelete="CASCADE"), nullable=False, index=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="favorites")
    restaurant = relationship("Restaurant")

class Notification(Base):
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    title = Column(String(255), nullable=False)
    message = Column(Text, nullable=False)
    notification_type = Column(String(50), default="ORDER_UPDATE") # ORDER_UPDATE, PROMO, PAYMENT, SECURITY
    reference_id = Column(String(100), nullable=True) # e.g. order_number
    read_status = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

class AuditLog(Base):
    __tablename__ = "audit_logs"

    id = Column(Integer, primary_key=True, index=True)
    actor_user_id = Column(Integer, ForeignKey("users.id"), nullable=True, index=True)
    action = Column(String(100), nullable=False) # ADMIN_LOGIN, REFUND_ISSUED, RESTAURANT_APPROVED, etc.
    resource_type = Column(String(50), nullable=False) # Order, Restaurant, User, Payment
    resource_id = Column(String(100), nullable=False)
    metadata_json = Column(Text, nullable=True)
    ip_address = Column(String(50), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)

class RewardPoint(Base):
    __tablename__ = "reward_points"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    points = Column(Integer, nullable=False)
    description = Column(String(255), nullable=False)
    is_credit = Column(Boolean, default=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

class SupportTicket(Base):
    __tablename__ = "support_tickets"

    id = Column(Integer, primary_key=True, index=True)
    ticket_id = Column(String(50), unique=True, index=True, nullable=False)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    order_number = Column(String(50), nullable=False)
    category = Column(String(100), nullable=False) # Missing Item, Late Delivery, Wrong Item, Payment Issue
    description = Column(Text, nullable=False)
    status = Column(String(50), default="In Progress", nullable=False) # In Progress, Resolved, Escalated
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

class GroupOrder(Base):
    __tablename__ = "group_orders"

    id = Column(Integer, primary_key=True, index=True)
    group_code = Column(String(50), unique=True, index=True, nullable=False)
    host_user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id"), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)

class GroupOrderParticipant(Base):
    __tablename__ = "group_order_participants"

    id = Column(Integer, primary_key=True, index=True)
    group_order_id = Column(Integer, ForeignKey("group_orders.id", ondelete="CASCADE"), nullable=False)
    participant_name = Column(String(100), nullable=False)
    item_subtotal = Column(Float, nullable=False)
    shared_charges = Column(Float, nullable=False)
    final_total = Column(Float, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
