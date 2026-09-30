package com.example.data.repository

import com.example.data.local.dao.CartDao
import com.example.data.local.entity.CartItemEntity
import com.example.domain.model.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val cartDao: CartDao) {

    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems().map { list ->
        list.map { entity ->
            CartItem(
                id = entity.id,
                menuItemId = entity.menuItemId,
                restaurantId = entity.restaurantId,
                name = entity.name,
                quantity = entity.quantity,
                unitPrice = entity.unitPrice,
                variantName = entity.variantName,
                addonsDescription = entity.addonsDescription,
                isVeg = entity.isVeg
            )
        }
    }

    suspend fun addItem(item: CartItem) {
        cartDao.insertCartItem(
            CartItemEntity(
                id = item.id,
                menuItemId = item.menuItemId,
                restaurantId = item.restaurantId,
                name = item.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                variantName = item.variantName,
                addonsDescription = item.addonsDescription,
                isVeg = item.isVeg
            )
        )
    }

    suspend fun updateQuantity(item: CartItem, newQty: Int) {
        if (newQty <= 0) {
            cartDao.deleteCartItem(
                CartItemEntity(
                    id = item.id,
                    menuItemId = item.menuItemId,
                    restaurantId = item.restaurantId,
                    name = item.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    variantName = item.variantName,
                    addonsDescription = item.addonsDescription,
                    isVeg = item.isVeg
                )
            )
        } else {
            cartDao.updateCartItem(
                CartItemEntity(
                    id = item.id,
                    menuItemId = item.menuItemId,
                    restaurantId = item.restaurantId,
                    name = item.name,
                    quantity = newQty,
                    unitPrice = item.unitPrice,
                    variantName = item.variantName,
                    addonsDescription = item.addonsDescription,
                    isVeg = item.isVeg
                )
            )
        }
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}
