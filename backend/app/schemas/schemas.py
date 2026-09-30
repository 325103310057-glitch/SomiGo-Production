from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
from datetime import datetime

# --- Auth & User ---
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class RegisterRequest(BaseModel):
    full_name: str
    email: EmailStr
    phone_number: str
    password: str
    role: Optional[str] = "CUSTOMER"

class OTPRequest(BaseModel):
    phone_number: str

class OTPVerifyRequest(BaseModel):
    phone_number: str
    otp_code: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    full_name: str
    role: str

class UserResponse(BaseModel):
    id: int
    full_name: str
    email: str
    phone_number: str
    role: str
    account_status: str
    created_at: datetime
    class Config:
        from_attributes = True

class AddressCreate(BaseModel):
    address_line: str
    apartment: Optional[str] = None
    landmark: Optional[str] = None
    city: str
    state: str
    postal_code: str
    latitude: float
    longitude: float
    address_type: str = "Home"
    is_default: bool = False

class AddressResponse(AddressCreate):
    id: int
    user_id: int
    created_at: datetime
    class Config:
        from_attributes = True

# --- Restaurant & Menu ---
class ItemAddonResponse(BaseModel):
    id: int
    addon_name: str
    price: float
    is_available: bool
    max_quantity: int
    class Config:
        from_attributes = True

class ItemVariantResponse(BaseModel):
    id: int
    variant_name: str
    price_delta: float
    is_available: bool
    class Config:
        from_attributes = True

class MenuItemResponse(BaseModel):
    id: int
    restaurant_id: int
    category_id: Optional[int] = None
    name: str
    description: Optional[str] = None
    image_url: Optional[str] = None
    price: float
    discounted_price: Optional[float] = None
    preparation_time: int
    vegetarian: bool
    spicy_level: int
    available: bool
    variants: List[ItemVariantResponse] = []
    addons: List[ItemAddonResponse] = []
    class Config:
        from_attributes = True

class CategoryResponse(BaseModel):
    id: int
    name: str
    image_url: Optional[str] = None
    display_order: int
    class Config:
        from_attributes = True

class RestaurantResponse(BaseModel):
    id: int
    restaurant_name: str
    description: Optional[str] = None
    phone: str
    logo_url: Optional[str] = None
    cover_image_url: Optional[str] = None
    address: str
    city: str
    state: str
    latitude: float
    longitude: float
    cuisine_type: str
    average_rating: float
    total_reviews: int
    minimum_order_value: float
    delivery_fee: float
    estimated_delivery_minutes: int
    is_open: bool
    class Config:
        from_attributes = True

# --- Cart ---
class CartItemInput(BaseModel):
    menu_item_id: int
    quantity: int = Field(gt=0)
    selected_variant: Optional[str] = None
    customization_data: Optional[str] = None

class CartItemResponse(BaseModel):
    id: int
    menu_item_id: int
    name: str
    quantity: int
    unit_price: float
    selected_variant: Optional[str] = None
    customization_data: Optional[str] = None
    total_price: float

class CartResponse(BaseModel):
    id: int
    restaurant_id: Optional[int] = None
    restaurant_name: Optional[str] = None
    items: List[CartItemResponse] = []
    subtotal: float = 0.0
    delivery_fee: float = 0.0
    taxes: float = 0.0
    platform_fee: float = 5.0
    discount: float = 0.0
    total_amount: float = 0.0

# --- Orders ---
class OrderCreate(BaseModel):
    restaurant_id: int
    delivery_address_id: int
    tip: float = 0.0
    coupon_code: Optional[str] = None
    payment_method: str = "UPI" # UPI, CARD, COD

class OrderItemResponse(BaseModel):
    id: int
    menu_item_id: Optional[int] = None
    item_name_snapshot: str
    quantity: int
    unit_price: float
    total_price: float
    customization_snapshot: Optional[str] = None
    class Config:
        from_attributes = True

class OrderResponse(BaseModel):
    id: int
    order_number: str
    customer_id: int
    restaurant_id: int
    restaurant_name: str
    delivery_address_line: str
    subtotal: float
    delivery_fee: float
    taxes: float
    discount: float
    platform_fee: float
    tip: float
    total_amount: float
    order_status: str
    payment_status: str
    estimated_delivery_time: Optional[datetime] = None
    placed_at: datetime
    items: List[OrderItemResponse] = []
    delivery_partner_name: Optional[str] = None
    delivery_partner_phone: Optional[str] = None
    class Config:
        from_attributes = True

# --- Payments ---
class PaymentCreateRequest(BaseModel):
    order_id: int
    payment_method: str = "UPI"

class PaymentWebhookPayload(BaseModel):
    event: str
    order_number: str
    transaction_id: str
    amount: float
    signature: str

# --- Reviews ---
class ReviewCreate(BaseModel):
    restaurant_id: int
    order_id: int
    rating: float = Field(ge=1.0, le=5.0)
    review_text: Optional[str] = None

class ReviewResponse(BaseModel):
    id: int
    rating: float
    review_text: Optional[str] = None
    created_at: datetime
    user_name: str
    restaurant_reply: Optional[str] = None
    class Config:
        from_attributes = True

# --- Admin Analytics ---
class AdminAnalyticsResponse(BaseModel):
    total_revenue_today: float
    total_revenue_weekly: float
    total_revenue_monthly: float
    total_orders_today: int
    total_orders_month: int
    active_customers: int
    completed_orders: int
    cancelled_orders: int
    average_order_value: float
    city_analytics: List[dict]
    popular_items: List[dict]
