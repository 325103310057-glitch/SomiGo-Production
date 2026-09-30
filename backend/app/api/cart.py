from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.api.auth import get_current_user
from app.models.user import User
from app.models.cart import Cart, CartItem
from app.models.menu import MenuItem
from app.models.restaurant import Restaurant
from app.schemas.schemas import CartItemInput, CartResponse, CartItemResponse

router = APIRouter(prefix="/cart", tags=["Cart"])

@router.get("/", response_model=CartResponse)
def get_cart(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    cart = db.query(Cart).filter(Cart.user_id == current_user.id).first()
    if not cart or not cart.items:
        return CartResponse(id=cart.id if cart else 0, restaurant_id=None, items=[])

    restaurant = db.query(Restaurant).filter(Restaurant.id == cart.restaurant_id).first()
    subtotal = 0.0
    items_response = []
    
    for ci in cart.items:
        item = ci.menu_item
        unit_price = item.discounted_price or item.price
        item_total = unit_price * ci.quantity
        subtotal += item_total
        items_response.append(CartItemResponse(
            id=ci.id,
            menu_item_id=ci.menu_item_id,
            name=item.name,
            quantity=ci.quantity,
            unit_price=unit_price,
            selected_variant=ci.selected_variant,
            customization_data=ci.customization_data,
            total_price=item_total
        ))

    delivery_fee = restaurant.delivery_fee if restaurant else 30.0
    taxes = round(subtotal * 0.05, 2)
    platform_fee = 5.0
    total_amount = round(subtotal + delivery_fee + taxes + platform_fee, 2)

    return CartResponse(
        id=cart.id,
        restaurant_id=cart.restaurant_id,
        restaurant_name=restaurant.restaurant_name if restaurant else None,
        items=items_response,
        subtotal=round(subtotal, 2),
        delivery_fee=delivery_fee,
        taxes=taxes,
        platform_fee=platform_fee,
        discount=0.0,
        total_amount=total_amount
    )

@router.post("/items")
def add_to_cart(item_input: CartItemInput, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    menu_item = db.query(MenuItem).filter(MenuItem.id == item_input.menu_item_id).first()
    if not menu_item or not menu_item.available:
        raise HTTPException(status_code=400, detail="Item is not available")

    cart = db.query(Cart).filter(Cart.user_id == current_user.id).first()
    if not cart:
        cart = Cart(user_id=current_user.id, restaurant_id=menu_item.restaurant_id)
        db.add(cart)
        db.commit()
        db.refresh(cart)
    
    # If adding from another restaurant, reset cart for new restaurant
    if cart.restaurant_id and cart.restaurant_id != menu_item.restaurant_id:
        db.query(CartItem).filter(CartItem.cart_id == cart.id).delete()
        cart.restaurant_id = menu_item.restaurant_id
        db.commit()

    existing_item = db.query(CartItem).filter(
        CartItem.cart_id == cart.id,
        CartItem.menu_item_id == menu_item.id,
        CartItem.selected_variant == item_input.selected_variant
    ).first()

    unit_price = menu_item.discounted_price or menu_item.price

    if existing_item:
        existing_item.quantity += item_input.quantity
    else:
        new_item = CartItem(
            cart_id=cart.id,
            menu_item_id=menu_item.id,
            quantity=item_input.quantity,
            selected_variant=item_input.selected_variant,
            customization_data=item_input.customization_data,
            unit_price=unit_price
        )
        db.add(new_item)

    db.commit()
    return {"message": "Item added to cart successfully"}

@router.delete("/items/{item_id}")
def remove_cart_item(item_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    cart = db.query(Cart).filter(Cart.user_id == current_user.id).first()
    if not cart:
        raise HTTPException(status_code=404, detail="Cart not found")
    
    item = db.query(CartItem).filter(CartItem.id == item_id, CartItem.cart_id == cart.id).first()
    if item:
        db.delete(item)
        db.commit()
    return {"message": "Item removed from cart"}

@router.delete("/clear")
def clear_cart(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    cart = db.query(Cart).filter(Cart.user_id == current_user.id).first()
    if cart:
        db.query(CartItem).filter(CartItem.cart_id == cart.id).delete()
        cart.restaurant_id = None
        db.commit()
    return {"message": "Cart cleared"}
