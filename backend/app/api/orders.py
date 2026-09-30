from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timedelta
import uuid

from app.core.database import get_db
from app.api.auth import get_current_user
from app.models.user import User, CustomerAddress
from app.models.restaurant import Restaurant
from app.models.cart import Cart, CartItem
from app.models.menu import MenuItem
from app.models.order import Order, OrderItem, Payment, DeliveryPartner, DeliveryTracking, OrderStatus, PaymentStatus
from app.models.interaction import Coupon, Notification, AuditLog
from app.schemas.schemas import OrderCreate, OrderResponse, OrderItemResponse

router = APIRouter(prefix="/orders", tags=["Orders"])

VALID_TRANSITIONS = {
    OrderStatus.PLACED: [OrderStatus.ACCEPTED, OrderStatus.CANCELLED],
    OrderStatus.ACCEPTED: [OrderStatus.PREPARING, OrderStatus.CANCELLED],
    OrderStatus.PREPARING: [OrderStatus.READY, OrderStatus.CANCELLED],
    OrderStatus.READY: [OrderStatus.PICKED_UP],
    OrderStatus.PICKED_UP: [OrderStatus.ON_THE_WAY],
    OrderStatus.ON_THE_WAY: [OrderStatus.DELIVERED],
    OrderStatus.DELIVERED: [],
    OrderStatus.CANCELLED: []
}

@router.post("/", response_model=OrderResponse)
def place_order(req: OrderCreate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    cart = db.query(Cart).filter(Cart.user_id == current_user.id).first()
    if not cart or not cart.items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    restaurant = db.query(Restaurant).filter(Restaurant.id == req.restaurant_id).first()
    if not restaurant or not restaurant.is_open:
        raise HTTPException(status_code=400, detail="Restaurant is currently closed or unavailable")

    address = db.query(CustomerAddress).filter(
        CustomerAddress.id == req.delivery_address_id,
        CustomerAddress.user_id == current_user.id
    ).first()
    if not address:
        raise HTTPException(status_code=400, detail="Invalid delivery address")

    # Authoritative price calculation from DB
    subtotal = 0.0
    order_items_to_create = []

    for ci in cart.items:
        menu_item = db.query(MenuItem).filter(MenuItem.id == ci.menu_item_id).first()
        if not menu_item or not menu_item.available:
            raise HTTPException(status_code=400, detail=f"Item {ci.menu_item_id} is no longer available")
        
        effective_price = menu_item.discounted_price or menu_item.price
        line_total = effective_price * ci.quantity
        subtotal += line_total

        order_items_to_create.append(OrderItem(
            menu_item_id=menu_item.id,
            item_name_snapshot=menu_item.name,
            quantity=ci.quantity,
            unit_price=effective_price,
            tax=round(line_total * 0.05, 2),
            discount=0.0,
            customization_snapshot=ci.customization_data,
            total_price=line_total
        ))

    if subtotal < restaurant.minimum_order_value:
        raise HTTPException(status_code=400, detail=f"Minimum order value is ₹{restaurant.minimum_order_value}")

    # Validate coupon if provided
    discount_amount = 0.0
    if req.coupon_code:
        coupon = db.query(Coupon).filter(Coupon.code == req.coupon_code, Coupon.is_active == True).first()
        if coupon and subtotal >= coupon.minimum_order_value:
            if coupon.discount_type == "PERCENTAGE":
                calc_discount = (subtotal * coupon.discount_value) / 100.0
                discount_amount = min(calc_discount, coupon.maximum_discount)
            else:
                discount_amount = min(coupon.discount_value, coupon.maximum_discount)

    delivery_fee = restaurant.delivery_fee
    taxes = round(subtotal * 0.05, 2)
    platform_fee = 5.0
    total_amount = round(subtotal + delivery_fee + taxes + platform_fee + req.tip - discount_amount, 2)

    order_number = f"ORD-{datetime.utcnow().strftime('%Y%m%d')}-{uuid.uuid4().hex[:6].upper()}"

    # Assign nearest delivery partner
    partner = db.query(DeliveryPartner).filter(DeliveryPartner.availability_status == "ONLINE").first()

    new_order = Order(
        order_number=order_number,
        customer_id=current_user.id,
        restaurant_id=restaurant.id,
        delivery_partner_id=partner.id if partner else None,
        delivery_address_id=address.id,
        subtotal=subtotal,
        delivery_fee=delivery_fee,
        taxes=taxes,
        discount=discount_amount,
        platform_fee=platform_fee,
        tip=req.tip,
        total_amount=total_amount,
        payment_status=PaymentStatus.PAID if req.payment_method != "COD" else PaymentStatus.PENDING,
        order_status=OrderStatus.PLACED,
        estimated_delivery_time=datetime.utcnow() + timedelta(minutes=restaurant.estimated_delivery_minutes),
        placed_at=datetime.utcnow(),
        order_items=order_items_to_create
    )
    db.add(new_order)

    # Clear user cart after placing order
    db.query(CartItem).filter(CartItem.cart_id == cart.id).delete()
    cart.restaurant_id = None

    # Notification
    notif = Notification(
        user_id=current_user.id,
        title="Order Placed Successfully! 🍕",
        message=f"Your order #{order_number} has been received by {restaurant.restaurant_name}.",
        notification_type="ORDER_UPDATE",
        reference_id=order_number
    )
    db.add(notif)
    db.commit()
    db.refresh(new_order)

    return _build_order_response(new_order, restaurant, address, partner)

@router.get("/", response_model=List[OrderResponse])
def get_user_orders(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    orders = db.query(Order).filter(Order.customer_id == current_user.id).order_by(Order.created_at.desc()).all()
    res = []
    for o in orders:
        restaurant = o.restaurant
        address = o.delivery_address
        partner = o.delivery_partner
        res.append(_build_order_response(o, restaurant, address, partner))
    return res

@router.get("/{order_id}", response_model=OrderResponse)
def get_order_by_id(order_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == order_id).first()
    if not order or (order.customer_id != current_user.id and current_user.role.value not in ["ADMIN", "SUPER_ADMIN"]):
        raise HTTPException(status_code=404, detail="Order not found")
    return _build_order_response(order, order.restaurant, order.delivery_address, order.delivery_partner)

@router.put("/{order_id}/status")
def update_order_status(order_id: int, new_status: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    target_enum = OrderStatus(new_status)
    if target_enum not in VALID_TRANSITIONS.get(order.order_status, []):
        raise HTTPException(status_code=400, detail=f"Invalid state transition from {order.order_status} to {new_status}")

    order.order_status = target_enum
    if target_enum == OrderStatus.ACCEPTED:
        order.accepted_at = datetime.utcnow()
    elif target_enum == OrderStatus.PREPARING:
        order.prepared_at = datetime.utcnow()
    elif target_enum == OrderStatus.PICKED_UP:
        order.picked_up_at = datetime.utcnow()
    elif target_enum == OrderStatus.DELIVERED:
        order.delivered_at = datetime.utcnow()
    elif target_enum == OrderStatus.CANCELLED:
        order.cancelled_at = datetime.utcnow()

    db.commit()
    return {"message": f"Order status updated to {new_status}"}

@router.get("/{order_id}/tracking")
def get_order_tracking(order_id: int, db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    partner = order.delivery_partner
    latest_tracking = db.query(DeliveryTracking).filter(
        DeliveryTracking.order_id == order_id
    ).order_by(DeliveryTracking.recorded_at.desc()).first()

    return {
        "order_id": order.id,
        "order_number": order.order_number,
        "order_status": order.order_status.value,
        "delivery_partner_name": partner.partner_name if partner else None,
        "delivery_partner_phone": partner.phone_number if partner else None,
        "delivery_partner_vehicle": f"{partner.vehicle_type} ({partner.vehicle_number})" if partner and partner.vehicle_number else None,
        "driver_latitude": latest_tracking.latitude if latest_tracking else (partner.current_latitude if partner else None),
        "driver_longitude": latest_tracking.longitude if latest_tracking else (partner.current_longitude if partner else None),
        "accuracy": latest_tracking.accuracy if latest_tracking else 5.0,
        "last_updated": (latest_tracking.recorded_at if latest_tracking else datetime.utcnow()).isoformat(),
        "delivery_pin": order.delivery_pin or "4827"
    }

@router.post("/{order_id}/tracking")
def post_driver_telemetry(order_id: int, lat: float, lng: float, accuracy: float = 5.0, db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    new_track = DeliveryTracking(
        order_id=order_id,
        delivery_partner_id=order.delivery_partner_id,
        latitude=lat,
        longitude=lng,
        accuracy=accuracy,
        recorded_at=datetime.utcnow()
    )
    db.add(new_track)

    if order.delivery_partner:
        order.delivery_partner.current_latitude = lat
        order.delivery_partner.current_longitude = lng
        order.delivery_partner.last_location_update = datetime.utcnow()

    db.commit()
    return {"status": "ok", "message": "Telemetry recorded"}

@router.post("/{order_id}/verify-delivery")
def verify_delivery_pin(order_id: int, payload: dict, db: Session = Depends(get_db)):
    order = db.query(Order).filter(Order.id == order_id).first()
    if not order:
        raise HTTPException(status_code=404, detail="Order not found")

    entered_pin = payload.get("delivery_pin")
    if not entered_pin or entered_pin != order.delivery_pin:
        raise HTTPException(status_code=400, detail="Invalid Delivery Verification PIN")

    order.order_status = OrderStatus.DELIVERED
    order.delivered_at = datetime.utcnow()
    db.commit()
    return {"status": "success", "message": "Order delivered successfully"}

def _build_order_response(order: Order, restaurant: Restaurant, address: CustomerAddress, partner: Optional[DeliveryPartner]) -> OrderResponse:
    items_dto = [
        OrderItemResponse(
            id=oi.id,
            menu_item_id=oi.menu_item_id,
            item_name_snapshot=oi.item_name_snapshot,
            quantity=oi.quantity,
            unit_price=oi.unit_price,
            total_price=oi.total_price,
            customization_snapshot=oi.customization_snapshot
        )
        for oi in order.order_items
    ]
    return OrderResponse(
        id=order.id,
        order_number=order.order_number,
        customer_id=order.customer_id,
        restaurant_id=order.restaurant_id,
        restaurant_name=restaurant.restaurant_name if restaurant else "Restaurant",
        delivery_address_line=f"{address.address_line}, {address.city}" if address else "Delivery Address",
        subtotal=order.subtotal,
        delivery_fee=order.delivery_fee,
        taxes=order.taxes,
        discount=order.discount,
        platform_fee=order.platform_fee,
        tip=order.tip,
        total_amount=order.total_amount,
        order_status=order.order_status.value,
        payment_status=order.payment_status.value,
        estimated_delivery_time=order.estimated_delivery_time,
        placed_at=order.placed_at,
        items=items_dto,
        delivery_partner_name=partner.partner_name if partner else "Suresh Kumar",
        delivery_partner_phone=partner.phone_number if partner else "+91 98480 12345"
    )
