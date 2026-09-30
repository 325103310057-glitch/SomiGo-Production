package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM cached_restaurants ORDER BY rating DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<RestaurantEntity>)

    @Query("DELETE FROM cached_restaurants")
    suspend fun clearAll()
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM local_orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE local_orders SET status = :status, progressStep = :step WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String, step: Int)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_restaurants")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorite_restaurants WHERE restaurantId = :restaurantId")
    suspend fun removeFavorite(restaurantId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_restaurants WHERE restaurantId = :restaurantId)")
    fun isFavorite(restaurantId: Int): Flow<Boolean>
}
