package com.example.data.remote

import com.example.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface BiteDashApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthTokenResponseDto>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthTokenResponseDto>

    @GET("restaurants/")
    suspend fun getRestaurants(
        @Query("city") city: String? = null,
        @Query("cuisine") cuisine: String? = null,
        @Query("search") search: String? = null,
        @Query("veg_only") vegOnly: Boolean = false
    ): Response<List<RestaurantDto>>

    @GET("restaurants/{id}")
    suspend fun getRestaurantById(@Path("id") id: Int): Response<RestaurantDto>

    @GET("restaurants/{id}/menu")
    suspend fun getRestaurantMenu(@Path("id") id: Int): Response<List<MenuItemDto>>

    @POST("orders/")
    suspend fun placeOrder(@Body request: OrderCreateDto): Response<OrderResponseDto>

    @GET("orders/")
    suspend fun getCustomerOrders(): Response<List<OrderResponseDto>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<OrderResponseDto>

    @GET("orders/{id}/tracking")
    suspend fun getOrderTracking(@Path("id") id: Int): Response<OrderTrackingDto>

    @POST("orders/{id}/tracking")
    suspend fun updateDriverTelemetry(
        @Path("id") id: Int,
        @Body request: DriverTelemetryRequestDto
    ): Response<Map<String, String>>

    @POST("orders/{id}/verify-delivery")
    suspend fun verifyDeliveryPin(
        @Path("id") id: Int,
        @Body request: DeliveryPinVerifyDto
    ): Response<Map<String, String>>
}
