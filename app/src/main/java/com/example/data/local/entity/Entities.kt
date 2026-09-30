package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val cuisine: String,
    val rating: Float,
    val totalReviews: Int,
    val deliveryMinutes: Int,
    val deliveryFee: Double,
    val minOrder: Double,
    val imageUrl: String,
    val city: String,
    val isOpen: Boolean = true,
    val offerTag: String? = null,
    val isPromoted: Boolean = false
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val menuItemId: Int,
    val restaurantId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantName: String? = null,
    val addonsDescription: String? = null,
    val isVeg: Boolean = true
)

@Entity(tableName = "local_orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val restaurantId: Int,
    val restaurantName: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val status: String,
    val placedAt: String,
    val deliveryAddress: String,
    val progressStep: Int = 0
)

@Entity(tableName = "favorite_restaurants")
data class FavoriteEntity(
    @PrimaryKey val restaurantId: Int,
    val restaurantName: String,
    val cuisine: String,
    val rating: Float,
    val imageUrl: String
)
