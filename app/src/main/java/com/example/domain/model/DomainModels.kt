package com.example.domain.model

data class Restaurant(
    val id: Int,
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

data class MenuItem(
    val id: Int,
    val restaurantId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val discountedPrice: Double? = null,
    val isVeg: Boolean = true,
    val spicyLevel: Int = 1,
    val imageUrl: String,
    val category: String,
    val variants: List<ItemVariant> = emptyList(),
    val addons: List<ItemAddon> = emptyList()
)

data class ItemVariant(
    val name: String,
    val priceDelta: Double
)

data class ItemAddon(
    val name: String,
    val price: Double
)

data class CartItem(
    val id: Int = 0,
    val menuItemId: Int,
    val restaurantId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantName: String? = null,
    val addonsDescription: String? = null,
    val isVeg: Boolean = true,
    val addedBy: String = "You"
) {
    val totalPrice: Double
        get() = unitPrice * quantity
}

enum class DeliveryStatus(val displayName: String) {
    PLACED("Order Placed"),
    ACCEPTED("Restaurant Accepted"),
    PREPARING("Preparing Food"),
    READY("Food Ready"),
    PICKED_UP("Order Picked Up"),
    ON_THE_WAY("On The Way"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

data class Order(
    val id: Int,
    val orderNumber: String,
    val restaurantId: Int,
    val restaurantName: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val taxes: Double,
    val discount: Double,
    val platformFee: Double = 5.0,
    val tip: Double = 0.0,
    val totalAmount: Double,
    val status: DeliveryStatus,
    val paymentStatus: String,
    val placedAt: String,
    val deliveryAddress: String,
    val deliveryPartnerName: String? = null,
    val deliveryPartnerPhone: String? = null,
    val deliveryPartnerVehicle: String? = null,
    val partnerRating: Float = 4.9f,
    val currentProgressStep: Int = 2, // 0 to 6
    val deliveryPin: String = "4827",
    val scheduledFor: String? = null,
    val isGroupOrder: Boolean = false,
    val restaurantLatitude: Double = 17.7200,
    val restaurantLongitude: Double = 83.3050,
    val customerLatitude: Double = 17.6868,
    val customerLongitude: Double = 83.2185,
    val deliveryPartnerLatitude: Double? = null,
    val deliveryPartnerLongitude: Double? = null,
    val deliveryPartnerAccuracyMeters: Float? = null,
    val hasLiveGpsSignal: Boolean = false
)

data class CustomerAddress(
    val id: Int,
    val label: String, // Home, Office, Other
    val addressLine: String,
    val city: String,
    val isDefault: Boolean = false
)

data class CityMetric(
    val city: String,
    val orders: Int,
    val revenue: Double
)

data class PopularFoodMetric(
    val name: String,
    val count: Int,
    val revenue: Double
)

data class AdminAnalytics(
    val todayRevenue: Double,
    val weeklyRevenue: Double,
    val monthlyRevenue: Double,
    val ordersToday: Int,
    val ordersMonth: Int,
    val activeCustomers: Int,
    val completedOrders: Int,
    val cancelledOrders: Int,
    val averageOrderValue: Double,
    val cityBreakdown: List<CityMetric>,
    val popularDishes: List<PopularFoodMetric>
)

// --- GROUP ORDER & SPLIT BILL ---
data class GroupParticipant(
    val name: String,
    val isHost: Boolean = false,
    val items: List<CartItem>,
    val subtotal: Double,
    val shareOfTaxesFees: Double,
    val finalTotal: Double
)

data class GroupOrderSession(
    val groupCode: String,
    val restaurantId: Int,
    val restaurantName: String,
    val hostName: String = "You (Host)",
    val participants: List<GroupParticipant>,
    val isLocked: Boolean = false
)

// --- LOYALTY & REWARDS ---
data class RewardTransaction(
    val id: String,
    val description: String,
    val points: Int,
    val isCredit: Boolean,
    val date: String
)

data class RewardAccount(
    val pointsBalance: Int = 250,
    val rupeeValue: Double = 125.0, // 2 points = ₹1
    val recentTransactions: List<RewardTransaction> = listOf(
        RewardTransaction("tx1", "Order #SG1002 Cashback", 35, true, "29 Sep"),
        RewardTransaction("tx2", "Welcome Signup Bonus", 150, true, "27 Sep"),
        RewardTransaction("tx3", "Weekend Feast Promo", 65, true, "28 Sep")
    )
)

// --- SUPPORT TICKETS ---
data class SupportTicket(
    val ticketId: String,
    val orderNumber: String,
    val category: String,
    val description: String,
    val status: String = "In Progress", // In Progress, Resolved, Escalated
    val createdAt: String
)

// --- "WHAT SHOULD I EAT" CRITERIA ---
data class FoodDiscoveryCriteria(
    val budgetMax: Double? = null,
    val isVegOnly: Boolean = false,
    val cuisine: String? = null,
    val spicyLevel: Int? = null, // 1: Mild, 2: Medium, 3: Spicy
    val maxDeliveryMinutes: Int? = null
)
