from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text, Enum
from sqlalchemy.orm import relationship
from datetime import datetime
import enum
from app.core.database import Base

class OrderStatus(str, enum.Enum):
    PLACED = "PLACED"
    ACCEPTED = "ACCEPTED"
    PREPARING = "PREPARING"
    READY = "READY"
    PICKED_UP = "PICKED_UP"
    ON_THE_WAY = "ON_THE_WAY"
    DELIVERED = "DELIVERED"
    CANCELLED = "CANCELLED"

class PaymentStatus(str, enum.Enum):
    PENDING = "PENDING"
    PAID = "PAID"
    FAILED = "FAILED"
    REFUNDED = "REFUNDED"

class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    order_number = Column(String(50), unique=True, index=True, nullable=False)
    customer_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id"), nullable=False, index=True)
    delivery_partner_id = Column(Integer, ForeignKey("delivery_partners.id"), nullable=True, index=True)
    delivery_address_id = Column(Integer, ForeignKey("customer_addresses.id"), nullable=False)
    
    subtotal = Column(Float, nullable=False)
    delivery_fee = Column(Float, default=0.0)
    taxes = Column(Float, default=0.0)
    discount = Column(Float, default=0.0)
    platform_fee = Column(Float, default=5.0)
    tip = Column(Float, default=0.0)
    total_amount = Column(Float, nullable=False)
    
    payment_status = Column(Enum(PaymentStatus), default=PaymentStatus.PENDING, nullable=False)
    order_status = Column(Enum(OrderStatus), default=OrderStatus.PLACED, nullable=False, index=True)
    
    estimated_delivery_time = Column(DateTime, nullable=True)
    placed_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    accepted_at = Column(DateTime, nullable=True)
    prepared_at = Column(DateTime, nullable=True)
    picked_up_at = Column(DateTime, nullable=True)
    delivered_at = Column(DateTime, nullable=True)
    cancelled_at = Column(DateTime, nullable=True)
    cancellation_reason = Column(String(255), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False, index=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    customer = relationship("User", foreign_keys=[customer_id], back_populates="orders")
    restaurant = relationship("Restaurant", back_populates="orders")
    delivery_partner = relationship("DeliveryPartner", back_populates="assigned_orders")
    delivery_address = relationship("CustomerAddress")
    order_items = relationship("OrderItem", back_populates="order", cascade="all, delete-orphan")
    payments = relationship("Payment", back_populates="order")
    tracking_points = relationship("DeliveryTracking", back_populates="order")

class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False, index=True)
    menu_item_id = Column(Integer, ForeignKey("menu_items.id"), nullable=True)
    item_name_snapshot = Column(String(255), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Float, nullable=False)
    tax = Column(Float, default=0.0)
    discount = Column(Float, default=0.0)
    customization_snapshot = Column(Text, nullable=True) # JSON snapshot of variant and add-ons
    total_price = Column(Float, nullable=False)

    order = relationship("Order", back_populates="order_items")

class Payment(Base):
    __tablename__ = "payments"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id"), nullable=False, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    payment_provider = Column(String(50), default="RAZORPAY") # RAZORPAY, PHONEPE, COD, MOCK
    transaction_id = Column(String(100), unique=True, index=True, nullable=False)
    amount = Column(Float, nullable=False)
    currency = Column(String(10), default="INR")
    payment_status = Column(Enum(PaymentStatus), default=PaymentStatus.PENDING, nullable=False)
    payment_method = Column(String(50), default="UPI") # UPI, CARD, NETBANKING, COD
    provider_response_reference = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    order = relationship("Order", back_populates="payments")

class DeliveryPartner(Base):
    __tablename__ = "delivery_partners"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False, unique=True)
    partner_name = Column(String(100), nullable=False)
    phone_number = Column(String(20), nullable=False)
    vehicle_type = Column(String(50), default="Bike") # Bike, EV, Scooter
    vehicle_number = Column(String(50), nullable=False)
    verification_status = Column(String(50), default="VERIFIED")
    availability_status = Column(String(50), default="ONLINE") # ONLINE, OFFLINE, ON_DELIVERY
    current_latitude = Column(Float, default=17.7231)
    current_longitude = Column(Float, default=83.3012)
    last_location_update = Column(DateTime, default=datetime.utcnow)
    rating = Column(Float, default=4.8)
    total_deliveries = Column(Integer, default=142)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    assigned_orders = relationship("Order", back_populates="delivery_partner")

class DeliveryTracking(Base):
    __tablename__ = "delivery_tracking"

    id = Column(Integer, primary_key=True, index=True)
    order_id = Column(Integer, ForeignKey("orders.id", ondelete="CASCADE"), nullable=False, index=True)
    delivery_partner_id = Column(Integer, ForeignKey("delivery_partners.id"), nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    recorded_at = Column(DateTime, default=datetime.utcnow, nullable=False)

    order = relationship("Order", back_populates="tracking_points")
