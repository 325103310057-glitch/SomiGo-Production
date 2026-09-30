from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text, Time
from sqlalchemy.orm import relationship
from datetime import datetime
from app.core.database import Base

class Restaurant(Base):
    __tablename__ = "restaurants"

    id = Column(Integer, primary_key=True, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    restaurant_name = Column(String(255), nullable=False, index=True)
    description = Column(Text, nullable=True)
    phone = Column(String(20), nullable=False)
    email = Column(String(255), nullable=True)
    logo_url = Column(String(500), nullable=True)
    cover_image_url = Column(String(500), nullable=True)
    address = Column(String(255), nullable=False)
    city = Column(String(100), nullable=False, index=True)
    state = Column(String(100), nullable=False)
    postal_code = Column(String(20), nullable=False)
    latitude = Column(Float, nullable=False, index=True)
    longitude = Column(Float, nullable=False, index=True)
    cuisine_type = Column(String(255), nullable=False) # e.g. "Biryani, North Indian, Mughlai"
    average_rating = Column(Float, default=4.2)
    total_reviews = Column(Integer, default=0)
    minimum_order_value = Column(Float, default=99.0)
    delivery_fee = Column(Float, default=30.0)
    estimated_delivery_minutes = Column(Integer, default=30)
    opening_time = Column(String(10), default="09:00")
    closing_time = Column(String(10), default="23:00")
    is_open = Column(Boolean, default=True)
    is_active = Column(Boolean, default=True)
    approval_status = Column(String(50), default="APPROVED") # PENDING, APPROVED, REJECTED
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    branches = relationship("RestaurantBranch", back_populates="restaurant")
    menu_items = relationship("MenuItem", back_populates="restaurant")
    orders = relationship("Order", back_populates="restaurant")
    reviews = relationship("Review", back_populates="restaurant")

class RestaurantBranch(Base):
    __tablename__ = "restaurant_branches"

    id = Column(Integer, primary_key=True, index=True)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id", ondelete="CASCADE"), nullable=False, index=True)
    branch_name = Column(String(255), nullable=False)
    address = Column(String(255), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    city = Column(String(100), nullable=False)
    state = Column(String(100), nullable=False)
    postal_code = Column(String(20), nullable=False)
    opening_time = Column(String(10), default="09:00")
    closing_time = Column(String(10), default="23:00")
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    restaurant = relationship("Restaurant", back_populates="branches")
