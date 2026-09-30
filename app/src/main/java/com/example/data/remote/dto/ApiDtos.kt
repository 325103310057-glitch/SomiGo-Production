package com.example.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "full_name") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "password") val password: String,
    @Json(name = "role") val role: String = "CUSTOMER"
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class AuthTokenResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "role") val role: String
)

@JsonClass(generateAdapter = true)
data class RestaurantDto(
    @Json(name = "id") val id: Int,
    @Json(name = "restaurant_name") val restaurantName: String,
    @Json(name = "description") val description: String?,
    @Json(name = "phone") val phone: String,
    @Json(name = "logo_url") val logoUrl: String?,
    @Json(name = "cover_image_url") val coverImageUrl: String?,
    @Json(name = "address") val address: String,
    @Json(name = "city") val city: String,
    @Json(name = "state") val state: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "cuisine_type") val cuisineType: String,
    @Json(name = "average_rating") val averageRating: Float,
    @Json(name = "total_reviews") val totalReviews: Int,
    @Json(name = "minimum_order_value") val minimumOrderValue: Double,
    @Json(name = "delivery_fee") val deliveryFee: Double,
    @Json(name = "estimated_delivery_minutes") val estimatedDeliveryMinutes: Int,
    @Json(name = "is_open") val isOpen: Boolean
)

@JsonClass(generateAdapter = true)
data class MenuItemDto(
    @Json(name = "id") val id: Int,
    @Json(name = "restaurant_id") val restaurantId: Int,
    @Json(name = "category_id") val categoryId: Int?,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "price") val price: Double,
    @Json(name = "discounted_price") val discountedPrice: Double?,
    @Json(name = "preparation_time") val preparationTime: Int,
    @Json(name = "vegetarian") val vegetarian: Boolean,
    @Json(name = "spicy_level") val spicyLevel: Int,
    @Json(name = "available") val available: Boolean
)

@JsonClass(generateAdapter = true)
data class OrderCreateDto(
    @Json(name = "restaurant_id") val restaurantId: Int,
    @Json(name = "delivery_address_id") val deliveryAddressId: Int,
    @Json(name = "tip") val tip: Double = 0.0,
    @Json(name = "coupon_code") val couponCode: String? = null,
    @Json(name = "payment_method") val paymentMethod: String = "UPI"
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @Json(name = "id") val id: Int,
    @Json(name = "menu_item_id") val menuItemId: Int?,
    @Json(name = "item_name_snapshot") val itemNameSnapshot: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price") val unitPrice: Double,
    @Json(name = "total_price") val totalPrice: Double
)

@JsonClass(generateAdapter = true)
data class OrderResponseDto(
    @Json(name = "id") val id: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "customer_id") val customerId: Int,
    @Json(name = "restaurant_id") val restaurantId: Int,
    @Json(name = "restaurant_name") val restaurantName: String,
    @Json(name = "delivery_address_line") val deliveryAddressLine: String,
    @Json(name = "subtotal") val subtotal: Double,
    @Json(name = "delivery_fee") val deliveryFee: Double,
    @Json(name = "taxes") val taxes: Double,
    @Json(name = "discount") val discount: Double,
    @Json(name = "platform_fee") val platformFee: Double,
    @Json(name = "tip") val tip: Double,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "order_status") val orderStatus: String,
    @Json(name = "payment_status") val paymentStatus: String,
    @Json(name = "placed_at") val placedAt: String,
    @Json(name = "items") val items: List<OrderItemDto> = emptyList(),
    @Json(name = "delivery_partner_name") val deliveryPartnerName: String?,
    @Json(name = "delivery_partner_phone") val deliveryPartnerPhone: String?
)

@JsonClass(generateAdapter = true)
data class OrderTrackingDto(
    @Json(name = "order_id") val orderId: Int,
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "order_status") val orderStatus: String,
    @Json(name = "delivery_partner_name") val deliveryPartnerName: String?,
    @Json(name = "delivery_partner_phone") val deliveryPartnerPhone: String?,
    @Json(name = "delivery_partner_vehicle") val deliveryPartnerVehicle: String?,
    @Json(name = "driver_latitude") val driverLatitude: Double?,
    @Json(name = "driver_longitude") val driverLongitude: Double?,
    @Json(name = "accuracy") val accuracy: Float?,
    @Json(name = "last_updated") val lastUpdated: String?,
    @Json(name = "delivery_pin") val deliveryPin: String
)

@JsonClass(generateAdapter = true)
data class DriverTelemetryRequestDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "accuracy") val accuracy: Float,
    @Json(name = "speed_kmh") val speedKmh: Float? = null
)

@JsonClass(generateAdapter = true)
data class DeliveryPinVerifyDto(
    @Json(name = "delivery_pin") val deliveryPin: String
)
