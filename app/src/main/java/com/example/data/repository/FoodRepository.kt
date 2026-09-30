package com.example.data.repository

import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.RestaurantDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.RestaurantEntity
import com.example.data.remote.BiteDashApiService
import com.example.data.remote.RetrofitClient
import com.example.domain.model.ItemAddon
import com.example.domain.model.ItemVariant
import com.example.domain.model.MenuItem
import com.example.domain.model.Restaurant
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val restaurantDao: RestaurantDao,
    private val favoriteDao: FavoriteDao,
    private val apiService: BiteDashApiService = RetrofitClient.apiService
) {
    // Initial catalog of curated premium food-delivery restaurants
    private val inMemoryRestaurants = listOf(
        Restaurant(
            id = 1,
            name = "Paradise Dum Biryani",
            cuisine = "Biryani, North Indian, Kebabs",
            rating = 4.6f,
            totalReviews = 1420,
            deliveryMinutes = 25,
            deliveryFee = 25.0,
            minOrder = 149.0,
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "60% OFF up to ₹120",
            isPromoted = true
        ),
        Restaurant(
            id = 2,
            name = "Crust & Craft Artisan Pizzeria",
            cuisine = "Italian, Woodfired Pizza, Pasta",
            rating = 4.8f,
            totalReviews = 890,
            deliveryMinutes = 30,
            deliveryFee = 35.0,
            minOrder = 199.0,
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "Flat ₹75 OFF",
            isPromoted = false
        ),
        Restaurant(
            id = 3,
            name = "Spice Symphony Kitchen",
            cuisine = "South Indian, Thali, Chettinad",
            rating = 4.4f,
            totalReviews = 620,
            deliveryMinutes = 20,
            deliveryFee = 0.0,
            minOrder = 99.0,
            imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "Free Delivery",
            isPromoted = true
        ),
        Restaurant(
            id = 4,
            name = "The Urban Burger Co.",
            cuisine = "American, Gourmet Burgers, Shakes",
            rating = 4.5f,
            totalReviews = 740,
            deliveryMinutes = 28,
            deliveryFee = 30.0,
            minOrder = 149.0,
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "Buy 1 Get 1 on Burgers",
            isPromoted = false
        ),
        Restaurant(
            id = 5,
            name = "Wok & Dragon Pan-Asian",
            cuisine = "Chinese, Momos, Thai Curries",
            rating = 4.7f,
            totalReviews = 1100,
            deliveryMinutes = 32,
            deliveryFee = 25.0,
            minOrder = 179.0,
            imageUrl = "https://images.unsplash.com/photo-1541696432-82c6da8ce7bf?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "20% OFF Above ₹299",
            isPromoted = false
        ),
        Restaurant(
            id = 6,
            name = "Sweet Truth Patisserie",
            cuisine = "Desserts, Cakes, Ice Creams",
            rating = 4.9f,
            totalReviews = 530,
            deliveryMinutes = 22,
            deliveryFee = 20.0,
            minOrder = 99.0,
            imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&auto=format&fit=crop&q=60",
            city = "Visakhapatnam",
            offerTag = "Complimentary Pastry",
            isPromoted = false
        )
    )

    private val inMemoryMenuItems = mapOf(
        1 to listOf(
            MenuItem(
                id = 101,
                restaurantId = 1,
                name = "Royal Hyderabadi Chicken Dum Biryani",
                description = "Fragrant long-grain basmati rice layered with spiced marinated chicken and slow-cooked in sealed handi.",
                price = 320.0,
                discountedPrice = 280.0,
                isVeg = false,
                spicyLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800&auto=format&fit=crop&q=60",
                category = "Biryani",
                variants = listOf(ItemVariant("Regular (Serves 1)", 0.0), ItemVariant("Family Pack (Serves 3)", 240.0)),
                addons = listOf(ItemAddon("Extra Salan & Raita", 30.0), ItemAddon("Boiled Egg (2 pcs)", 25.0))
            ),
            MenuItem(
                id = 102,
                restaurantId = 1,
                name = "Nizami Paneer Dum Biryani",
                description = "Fresh cottage cheese cubes marinated in aromatic spices and saffron infused basmati rice.",
                price = 260.0,
                discountedPrice = 230.0,
                isVeg = true,
                spicyLevel = 1,
                imageUrl = "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=800&auto=format&fit=crop&q=60",
                category = "Biryani",
                variants = listOf(ItemVariant("Regular", 0.0), ItemVariant("Large", 180.0)),
                addons = listOf(ItemAddon("Extra Roasted Cashews", 40.0))
            ),
            MenuItem(
                id = 103,
                restaurantId = 1,
                name = "Murgh Malai Tikka (6 Pcs)",
                description = "Mouth-melting boneless chicken chunks marinated in cream, cheese, and mild spices roasted in clay oven.",
                price = 310.0,
                isVeg = false,
                spicyLevel = 1,
                imageUrl = "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800&auto=format&fit=crop&q=60",
                category = "Starters",
                addons = listOf(ItemAddon("Mint Chutney & Salad", 20.0))
            )
        ),
        2 to listOf(
            MenuItem(
                id = 201,
                restaurantId = 2,
                name = "Woodfired Margherita Pizza",
                description = "San Marzano tomato sauce, fresh buffalo mozzarella, fresh basil, and extra virgin olive oil.",
                price = 340.0,
                discountedPrice = 299.0,
                isVeg = true,
                spicyLevel = 1,
                imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&auto=format&fit=crop&q=60",
                category = "Pizzas",
                variants = listOf(ItemVariant("10 inch (Regular)", 0.0), ItemVariant("12 inch (Large)", 150.0)),
                addons = listOf(ItemAddon("Extra Mozzarella", 60.0), ItemAddon("Garlic Butter Crust", 40.0))
            ),
            MenuItem(
                id = 202,
                restaurantId = 2,
                name = "Smoky BBQ Chicken Feast Pizza",
                description = "Slow-smoked barbecue chicken, red onions, bell peppers, melted cheddar and fresh herbs.",
                price = 420.0,
                discountedPrice = 370.0,
                isVeg = false,
                spicyLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&auto=format&fit=crop&q=60",
                category = "Pizzas",
                variants = listOf(ItemVariant("10 inch", 0.0), ItemVariant("12 inch", 160.0)),
                addons = listOf(ItemAddon("Peri Peri Dip", 35.0), ItemAddon("Cheese Stuffed Crust", 70.0))
            )
        ),
        3 to listOf(
            MenuItem(
                id = 301,
                restaurantId = 3,
                name = "Ghee Roast Masala Dosa",
                description = "Crispy golden fermented crepe roasted with pure desi ghee, filled with spiced mashed potato filling.",
                price = 140.0,
                discountedPrice = 120.0,
                isVeg = true,
                spicyLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=800&auto=format&fit=crop&q=60",
                category = "Tiffins",
                addons = listOf(ItemAddon("Extra Podi & Ghee", 25.0), ItemAddon("Filter Coffee", 40.0))
            )
        ),
        4 to listOf(
            MenuItem(
                id = 401,
                restaurantId = 4,
                name = "The Double Truffle Smash Burger",
                description = "Double smashed seasoned patty, melted aged cheddar, caramelized onions, and house black truffle aioli.",
                price = 320.0,
                discountedPrice = 280.0,
                isVeg = false,
                spicyLevel = 1,
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop&q=60",
                category = "Burgers",
                variants = listOf(ItemVariant("Single Patty", -50.0), ItemVariant("Double Patty", 0.0)),
                addons = listOf(ItemAddon("Cajun Fries", 70.0), ItemAddon("Cheese Dip", 40.0))
            )
        ),
        5 to listOf(
            MenuItem(
                id = 501,
                restaurantId = 5,
                name = "Steamed Chicken Dimsums (8 Pcs)",
                description = "Delicate translucent wrappers filled with minced scallion chicken, served with spicy firecracker sauce.",
                price = 240.0,
                discountedPrice = 210.0,
                isVeg = false,
                spicyLevel = 2,
                imageUrl = "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=800&auto=format&fit=crop&q=60",
                category = "Dimsums",
                addons = listOf(ItemAddon("Chili Oil Dip", 25.0))
            )
        ),
        6 to listOf(
            MenuItem(
                id = 601,
                restaurantId = 6,
                name = "Molten Belgian Dark Chocolate Cake",
                description = "Warm decadent chocolate cake with a molten oozing lava center.",
                price = 180.0,
                discountedPrice = 150.0,
                isVeg = true,
                spicyLevel = 0,
                imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800&auto=format&fit=crop&q=60",
                category = "Desserts",
                addons = listOf(ItemAddon("Vanilla Bean Scoop", 45.0))
            )
        )
    )

    fun getRestaurants(): List<Restaurant> = inMemoryRestaurants

    fun getRestaurantById(id: Int): Restaurant? = inMemoryRestaurants.find { it.id == id }

    fun getMenuItems(restaurantId: Int): List<MenuItem> = inMemoryMenuItems[restaurantId] ?: emptyList()

    fun searchRestaurants(query: String, vegOnly: Boolean = false, maxMinutes: Int? = null): List<Restaurant> {
        return inMemoryRestaurants.filter { r ->
            val matchesQuery = query.isBlank() || 
                r.name.contains(query, ignoreCase = true) || 
                r.cuisine.contains(query, ignoreCase = true)
            val matchesTime = maxMinutes == null || r.deliveryMinutes <= maxMinutes
            matchesQuery && matchesTime
        }
    }

    val favorites: Flow<List<FavoriteEntity>> = favoriteDao.getFavorites()

    suspend fun toggleFavorite(restaurant: Restaurant, isFav: Boolean) {
        if (isFav) {
            favoriteDao.removeFavorite(restaurant.id)
        } else {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    restaurantId = restaurant.id,
                    restaurantName = restaurant.name,
                    cuisine = restaurant.cuisine,
                    rating = restaurant.rating,
                    imageUrl = restaurant.imageUrl
                )
            )
        }
    }

    suspend fun syncWithCloudBackend() {
        try {
            val response = apiService.getRestaurants()
            if (response.isSuccessful && response.body() != null) {
                val cloudRestaurants = response.body()!!.map { dto ->
                    RestaurantEntity(
                        id = dto.id,
                        name = dto.restaurantName,
                        cuisine = dto.cuisineType,
                        rating = dto.averageRating,
                        totalReviews = dto.totalReviews,
                        deliveryMinutes = dto.estimatedDeliveryMinutes,
                        deliveryFee = dto.deliveryFee,
                        minOrder = dto.minimumOrderValue,
                        imageUrl = dto.coverImageUrl ?: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800",
                        city = dto.city,
                        isOpen = dto.isOpen
                    )
                }
                restaurantDao.insertAll(cloudRestaurants)
            }
        } catch (e: Exception) {
            android.util.Log.d("FoodRepository", "Using local cache: ${e.message}")
        }
    }
}
