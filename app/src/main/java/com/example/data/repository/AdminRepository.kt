package com.example.data.repository

import com.example.domain.model.AdminAnalytics
import com.example.domain.model.CityMetric
import com.example.domain.model.PopularFoodMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminRepository {

    private val _analytics = MutableStateFlow(
        AdminAnalytics(
            todayRevenue = 0.0,
            weeklyRevenue = 0.0,
            monthlyRevenue = 0.0,
            ordersToday = 0,
            ordersMonth = 0,
            activeCustomers = 0,
            completedOrders = 0,
            cancelledOrders = 0,
            averageOrderValue = 0.0,
            cityBreakdown = listOf(
                CityMetric("Visakhapatnam", 0, 0.0),
                CityMetric("Hyderabad", 0, 0.0),
                CityMetric("Vijayawada", 0, 0.0),
                CityMetric("Bengaluru", 0, 0.0)
            ),
            popularDishes = emptyList()
        )
    )

    val analytics: Flow<AdminAnalytics> = _analytics.asStateFlow()

    fun updateMetricsFromOrders(orderCount: Int, totalRevenue: Double) {
        val current = _analytics.value
        _analytics.value = current.copy(
            todayRevenue = totalRevenue,
            ordersToday = orderCount,
            completedOrders = orderCount,
            averageOrderValue = if (orderCount > 0) totalRevenue / orderCount else 0.0
        )
    }
}
