from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from app.core.database import get_db
from app.models.restaurant import Restaurant
from app.models.menu import Category, MenuItem
from app.schemas.schemas import RestaurantResponse, CategoryResponse, MenuItemResponse

router = APIRouter(prefix="/restaurants", tags=["Restaurants & Menus"])

@router.get("/categories", response_model=List[CategoryResponse])
def get_categories(db: Session = Depends(get_db)):
    return db.query(Category).filter(Category.is_active == True).order_by(Category.display_order.asc()).all()

@router.get("/", response_model=List[RestaurantResponse])
def get_restaurants(
    city: Optional[str] = None,
    cuisine: Optional[str] = None,
    min_rating: Optional[float] = None,
    search: Optional[str] = None,
    veg_only: bool = False,
    db: Session = Depends(get_db)
):
    query = db.query(Restaurant).filter(Restaurant.is_active == True, Restaurant.approval_status == "APPROVED")
    
    if city:
        query = query.filter(Restaurant.city.ilike(f"%{city}%"))
    if cuisine:
        query = query.filter(Restaurant.cuisine_type.ilike(f"%{cuisine}%"))
    if min_rating:
        query = query.filter(Restaurant.average_rating >= min_rating)
    if search:
        search_filter = f"%{search}%"
        query = query.filter(
            (Restaurant.restaurant_name.ilike(search_filter)) |
            (Restaurant.cuisine_type.ilike(search_filter)) |
            (Restaurant.description.ilike(search_filter))
        )
    return query.order_by(Restaurant.average_rating.desc()).limit(50).all()

@router.get("/{restaurant_id}", response_model=RestaurantResponse)
def get_restaurant_by_id(restaurant_id: int, db: Session = Depends(get_db)):
    restaurant = db.query(Restaurant).filter(Restaurant.id == restaurant_id).first()
    if not restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")
    return restaurant

@router.get("/{restaurant_id}/menu", response_model=List[MenuItemResponse])
def get_restaurant_menu(restaurant_id: int, db: Session = Depends(get_db)):
    restaurant = db.query(Restaurant).filter(Restaurant.id == restaurant_id).first()
    if not restaurant:
        raise HTTPException(status_code=404, detail="Restaurant not found")
    items = db.query(MenuItem).filter(MenuItem.restaurant_id == restaurant_id, MenuItem.available == True).all()
    return items
