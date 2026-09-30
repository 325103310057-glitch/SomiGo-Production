from sqlalchemy import Column, Integer, String, Boolean, DateTime, Float, ForeignKey, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from app.core.database import Base

class Category(Base):
    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False, unique=True, index=True)
    image_url = Column(String(500), nullable=True)
    is_active = Column(Boolean, default=True)
    display_order = Column(Integer, default=0)

    menu_items = relationship("MenuItem", back_populates="category")

class MenuItem(Base):
    __tablename__ = "menu_items"

    id = Column(Integer, primary_key=True, index=True)
    restaurant_id = Column(Integer, ForeignKey("restaurants.id", ondelete="CASCADE"), nullable=False, index=True)
    category_id = Column(Integer, ForeignKey("categories.id"), nullable=True, index=True)
    name = Column(String(255), nullable=False, index=True)
    description = Column(Text, nullable=True)
    image_url = Column(String(500), nullable=True)
    price = Column(Float, nullable=False)
    discounted_price = Column(Float, nullable=True)
    tax_percentage = Column(Float, default=5.0)
    preparation_time = Column(Integer, default=15) # minutes
    vegetarian = Column(Boolean, default=True)
    vegan = Column(Boolean, default=False)
    spicy_level = Column(Integer, default=1) # 1 mild, 2 medium, 3 spicy
    available = Column(Boolean, default=True)
    is_featured = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    restaurant = relationship("Restaurant", back_populates="menu_items")
    category = relationship("Category", back_populates="menu_items")
    variants = relationship("ItemVariant", back_populates="menu_item", cascade="all, delete-orphan")
    addons = relationship("ItemAddon", back_populates="menu_item", cascade="all, delete-orphan")

class ItemVariant(Base):
    __tablename__ = "item_variants"

    id = Column(Integer, primary_key=True, index=True)
    menu_item_id = Column(Integer, ForeignKey("menu_items.id", ondelete="CASCADE"), nullable=False, index=True)
    variant_name = Column(String(100), nullable=False) # Small, Medium, Large, Half, Full
    price_delta = Column(Float, default=0.0) # Additional or base delta
    is_available = Column(Boolean, default=True)

    menu_item = relationship("MenuItem", back_populates="variants")

class ItemAddon(Base):
    __tablename__ = "item_addons"

    id = Column(Integer, primary_key=True, index=True)
    menu_item_id = Column(Integer, ForeignKey("menu_items.id", ondelete="CASCADE"), nullable=False, index=True)
    addon_name = Column(String(100), nullable=False) # Extra Cheese, Extra Dip, Extra Raita
    price = Column(Float, default=0.0)
    is_available = Column(Boolean, default=True)
    max_quantity = Column(Integer, default=3)

    menu_item = relationship("MenuItem", back_populates="addons")
