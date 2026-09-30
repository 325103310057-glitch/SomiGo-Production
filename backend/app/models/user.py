from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text, Enum
from sqlalchemy.orm import relationship
from datetime import datetime
import enum
from app.core.database import Base

class UserRole(str, enum.Enum):
    CUSTOMER = "CUSTOMER"
    RESTAURANT_OWNER = "RESTAURANT_OWNER"
    DELIVERY_PARTNER = "DELIVERY_PARTNER"
    ADMIN = "ADMIN"
    SUPER_ADMIN = "SUPER_ADMIN"
    SUPPORT_AGENT = "SUPPORT_AGENT"

class AccountStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    SUSPENDED = "SUSPENDED"
    DEACTIVATED = "DEACTIVATED"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, index=True, nullable=False)
    phone_number = Column(String(20), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=True)
    profile_image_url = Column(String(500), nullable=True)
    date_of_birth = Column(String(50), nullable=True)
    role = Column(Enum(UserRole), default=UserRole.CUSTOMER, nullable=False)
    account_status = Column(Enum(AccountStatus), default=AccountStatus.ACTIVE, nullable=False)
    email_verified = Column(Boolean, default=False)
    phone_verified = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)
    last_login_at = Column(DateTime, nullable=True)

    # Relationships
    addresses = relationship("CustomerAddress", back_populates="user", cascade="all, delete-orphan")
    orders = relationship("Order", back_populates="customer", foreign_keys="Order.customer_id")
    reviews = relationship("Review", back_populates="user")
    favorites = relationship("Favorite", back_populates="user")

class CustomerAddress(Base):
    __tablename__ = "customer_addresses"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    address_line = Column(String(255), nullable=False)
    apartment = Column(String(100), nullable=True)
    landmark = Column(String(100), nullable=True)
    city = Column(String(100), nullable=False, index=True)
    state = Column(String(100), nullable=False)
    postal_code = Column(String(20), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    address_type = Column(String(50), default="Home") # Home, Work, Other
    is_default = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    user = relationship("User", back_populates="addresses")
