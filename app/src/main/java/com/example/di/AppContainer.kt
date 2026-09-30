package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.AdminRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.FoodRepository
import com.example.data.repository.OrderRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context)

    val foodRepository = FoodRepository(
        restaurantDao = database.restaurantDao(),
        favoriteDao = database.favoriteDao()
    )

    val cartRepository = CartRepository(
        cartDao = database.cartDao()
    )

    val orderRepository = OrderRepository(
        orderDao = database.orderDao()
    )

    val adminRepository = AdminRepository()
}
