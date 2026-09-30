package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdminRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.FoodRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ---------------- HOME VIEWMODEL ----------------
class HomeViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _vegOnlyFilter = MutableStateFlow(false)
    val vegOnlyFilter = _vegOnlyFilter.asStateFlow()

    private val _fastDeliveryFilter = MutableStateFlow(false)
    val fastDeliveryFilter = _fastDeliveryFilter.asStateFlow()

    private val _selectedCity = MutableStateFlow("Visakhapatnam, Andhra Pradesh")
    val selectedCity = _selectedCity.asStateFlow()

    // "What Should I Eat?" discovery assistant criteria
    private val _assistantCriteria = MutableStateFlow(FoodDiscoveryCriteria())
    val assistantCriteria = _assistantCriteria.asStateFlow()

    val restaurants: StateFlow<List<Restaurant>> = combine(
        _selectedCategory,
        _searchQuery,
        _vegOnlyFilter,
        _fastDeliveryFilter
    ) { category, query, vegOnly, fastDelivery ->
        foodRepository.getRestaurants().filter { r ->
            val matchCat = category == null || category == "All" || r.cuisine.contains(category, ignoreCase = true)
            val matchQuery = query.isBlank() || r.name.contains(query, ignoreCase = true) || r.cuisine.contains(query, ignoreCase = true)
            val matchFast = !fastDelivery || r.deliveryMinutes <= 25
            matchCat && matchQuery && matchFast
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), foodRepository.getRestaurants())

    fun selectCategory(cat: String) {
        _selectedCategory.value = if (_selectedCategory.value == cat) "All" else cat
    }

    fun onSearchChange(q: String) {
        _searchQuery.value = q
    }

    fun toggleVegFilter() {
        _vegOnlyFilter.value = !_vegOnlyFilter.value
    }

    fun toggleFastDelivery() {
        _fastDeliveryFilter.value = !_fastDeliveryFilter.value
    }

    fun setCity(city: String) {
        _selectedCity.value = city
    }

    fun toggleFavorite(restaurant: Restaurant, isFav: Boolean) {
        viewModelScope.launch {
            foodRepository.toggleFavorite(restaurant, isFav)
        }
    }

    val favorites = foodRepository.favorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // "What Should I Eat?" Rule-Based Engine
    fun updateAssistantCriteria(
        budgetMax: Double? = null,
        isVegOnly: Boolean? = null,
        cuisine: String? = null,
        spicyLevel: Int? = null
    ) {
        _assistantCriteria.value = _assistantCriteria.value.copy(
            budgetMax = budgetMax ?: _assistantCriteria.value.budgetMax,
            isVegOnly = isVegOnly ?: _assistantCriteria.value.isVegOnly,
            cuisine = cuisine ?: _assistantCriteria.value.cuisine,
            spicyLevel = spicyLevel ?: _assistantCriteria.value.spicyLevel
        )
    }

    fun getRecommendedDishes(): List<Pair<MenuItem, Restaurant>> {
        val crit = _assistantCriteria.value
        val allRestaurants = foodRepository.getRestaurants()
        val results = mutableListOf<Pair<MenuItem, Restaurant>>()

        for (restaurant in allRestaurants) {
            val items = foodRepository.getMenuItems(restaurant.id)
            for (item in items) {
                val matchesVeg = !crit.isVegOnly || item.isVeg
                val matchesBudget = crit.budgetMax == null || (item.discountedPrice ?: item.price) <= crit.budgetMax!!
                val matchesCuisine = crit.cuisine == null || crit.cuisine == "All" || 
                                     restaurant.cuisine.contains(crit.cuisine!!, ignoreCase = true) ||
                                     item.category.contains(crit.cuisine!!, ignoreCase = true)
                val matchesSpice = crit.spicyLevel == null || item.spicyLevel <= crit.spicyLevel!!

                if (matchesVeg && matchesBudget && matchesCuisine && matchesSpice) {
                    results.add(Pair(item, restaurant))
                }
            }
        }
        return if (results.isNotEmpty()) results.take(6) else {
            // Default top recommendations
            val r1 = allRestaurants.firstOrNull() ?: return emptyList()
            foodRepository.getMenuItems(r1.id).take(4).map { Pair(it, r1) }
        }
    }

    // Trending Dishes Section for discovery feed
    fun getTrendingDishes(): List<Pair<MenuItem, Restaurant>> {
        val allRestaurants = foodRepository.getRestaurants()
        val list = mutableListOf<Pair<MenuItem, Restaurant>>()
        for (r in allRestaurants) {
            val items = foodRepository.getMenuItems(r.id)
            if (items.isNotEmpty()) {
                list.add(Pair(items.first(), r))
            }
        }
        return list.take(5)
    }
}

// ---------------- CART VIEWMODEL (WITH GROUP ORDER & SPLIT BILL) ----------------
class CartViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon = _appliedCoupon.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount = _discountAmount.asStateFlow()

    private val _selectedTip = MutableStateFlow(20.0)
    val selectedTip = _selectedTip.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("UPI")
    val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("Flat 402, Sea Pearl Heights, Beach Road, Visakhapatnam")
    val deliveryAddress = _deliveryAddress.asStateFlow()

    // SomiGo Coins Loyalty
    private val _useRewardsCoins = MutableStateFlow(false)
    val useRewardsCoins = _useRewardsCoins.asStateFlow()
    val rewardsCoinsDiscount = 50.0 // 100 coins = ₹50 discount

    // Scheduled Order
    private val _scheduledOption = MutableStateFlow("Instant Delivery (~25 mins)")
    val scheduledOption = _scheduledOption.asStateFlow()

    // Group Order & Split Bill
    private val _isGroupOrder = MutableStateFlow(false)
    val isGroupOrder = _isGroupOrder.asStateFlow()

    private val _groupCode = MutableStateFlow<String?>(null)
    val groupCode = _groupCode.asStateFlow()

    private val _simulatedFriends = MutableStateFlow<List<Pair<String, List<CartItem>>>>(emptyList())
    val simulatedFriends = _simulatedFriends.asStateFlow()

    fun addItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.addItem(item)
        }
    }

    fun updateQuantity(item: CartItem, newQty: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(item, newQty)
        }
    }

    fun applyCoupon(code: String) {
        val uppercase = code.trim().uppercase()
        if (uppercase == "SOMIGO50" || uppercase == "BITE50") {
            _appliedCoupon.value = "SOMIGO50"
            _discountAmount.value = 100.0
        } else if (uppercase == "WELCOME" || uppercase == "SOMIGO75") {
            _appliedCoupon.value = "SOMIGO75"
            _discountAmount.value = 75.0
        } else {
            _appliedCoupon.value = null
            _discountAmount.value = 0.0
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _discountAmount.value = 0.0
    }

    fun selectTip(tip: Double) {
        _selectedTip.value = tip
    }

    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun setDeliveryAddress(addr: String) {
        _deliveryAddress.value = addr
    }

    fun toggleUseRewardsCoins() {
        _useRewardsCoins.value = !_useRewardsCoins.value
    }

    fun setScheduleOption(opt: String) {
        _scheduledOption.value = opt
    }

    // Group Order actions
    fun startGroupOrder() {
        _isGroupOrder.value = true
        _groupCode.value = "SG-" + (1000 + (Math.random() * 9000).toInt())
        // Add dynamic friend entries so users can test Split Bill
        _simulatedFriends.value = listOf(
            Pair("Kiran", listOf(
                CartItem(menuItemId = 901, restaurantId = 1, name = "Nizami Paneer Dum Biryani", quantity = 1, unitPrice = 230.0, addedBy = "Kiran")
            )),
            Pair("Priya", listOf(
                CartItem(menuItemId = 902, restaurantId = 1, name = "Murgh Malai Tikka", quantity = 1, unitPrice = 310.0, addedBy = "Priya")
            ))
        )
    }

    fun cancelGroupOrder() {
        _isGroupOrder.value = false
        _groupCode.value = null
        _simulatedFriends.value = emptyList()
    }

    // Split Bill Calculations
    fun getSplitBillBreakdown(): List<GroupParticipant> {
        val userItems = cartItems.value
        val friends = _simulatedFriends.value

        val userSubtotal = userItems.sumOf { it.totalPrice }
        val friendsSubtotal = friends.sumOf { it.second.sumOf { item -> item.totalPrice } }
        val totalSubtotal = userSubtotal + friendsSubtotal

        val totalParticipants = 1 + friends.size
        val sharedCharges = (25.0 + (totalSubtotal * 0.05) + 5.0) / totalParticipants // Delivery + Taxes + Platform fee split evenly

        val list = mutableListOf<GroupParticipant>()
        list.add(
            GroupParticipant(
                name = "You (Host)",
                isHost = true,
                items = userItems,
                subtotal = userSubtotal,
                shareOfTaxesFees = Math.round(sharedCharges * 10.0) / 10.0,
                finalTotal = Math.round((userSubtotal + sharedCharges) * 10.0) / 10.0
            )
        )
        for (f in friends) {
            val fSub = f.second.sumOf { it.totalPrice }
            list.add(
                GroupParticipant(
                    name = f.first,
                    isHost = false,
                    items = f.second,
                    subtotal = fSub,
                    shareOfTaxesFees = Math.round(sharedCharges * 10.0) / 10.0,
                    finalTotal = Math.round((fSub + sharedCharges) * 10.0) / 10.0
                )
            )
        }
        return list
    }

    suspend fun placeOrder(restaurantName: String, onOrderPlaced: (Order) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) return

        val userSubtotal = items.sumOf { it.totalPrice }
        val friendsSubtotal = if (_isGroupOrder.value) _simulatedFriends.value.sumOf { it.second.sumOf { i -> i.totalPrice } } else 0.0
        val subtotal = userSubtotal + friendsSubtotal

        val deliveryFee = 25.0
        val taxes = Math.round(subtotal * 0.05 * 10.0) / 10.0
        val discount = _discountAmount.value + (if (_useRewardsCoins.value) rewardsCoinsDiscount else 0.0)
        val tip = _selectedTip.value
        val platformFee = 5.0
        val total = Math.max(0.0, subtotal + deliveryFee + taxes + platformFee + tip - discount)

        val scheduledText = if (_scheduledOption.value.contains("Instant")) null else _scheduledOption.value

        val order = orderRepository.placeOrder(
            restaurantId = items.first().restaurantId,
            restaurantName = restaurantName,
            items = items,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            taxes = taxes,
            discount = discount,
            tip = tip,
            totalAmount = total,
            deliveryAddress = _deliveryAddress.value
        ).copy(
            scheduledFor = scheduledText,
            isGroupOrder = _isGroupOrder.value
        )

        cartRepository.clearCart()
        _appliedCoupon.value = null
        _discountAmount.value = 0.0
        _useRewardsCoins.value = false
        _isGroupOrder.value = false
        onOrderPlaced(order)
    }
}

// ---------------- ORDER VIEWMODEL (WITH DELIVERY PIN & REORDER) ----------------
class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    val activeOrder = orderRepository.activeOrder

    val ordersHistory: StateFlow<List<Order>> = orderRepository.ordersHistory.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun setActiveOrder(order: Order) {
        orderRepository.setActiveOrder(order)
    }

    fun verifyDeliveryPin(enteredPin: String, onComplete: (Boolean) -> Unit) {
        val current = activeOrder.value ?: return
        viewModelScope.launch {
            val result = orderRepository.verifyDeliveryPin(current.id, enteredPin)
            onComplete(result)
        }
    }

    // Reorder: checks current items & repopulates the cart
    fun reorder(order: Order, cartViewModel: CartViewModel) {
        viewModelScope.launch {
            for (item in order.items) {
                cartViewModel.addItem(item)
            }
        }
    }
}

// ---------------- SUPPORT & REWARDS VIEWMODEL ----------------
class SupportViewModel : ViewModel() {

    private val _rewardAccount = MutableStateFlow(RewardAccount())
    val rewardAccount = _rewardAccount.asStateFlow()

    private val _supportTickets = MutableStateFlow(
        listOf(
            SupportTicket("SG10284", "ORD1002", "Late Delivery", "Delivery was delayed by 15 minutes due to heavy rain.", "Resolved", "29 Sep, 05:40 PM"),
            SupportTicket("SG10341", "ORD1003", "Missing Item", "Requested extra mint dip was missing from order handoff.", "In Progress", "30 Sep, 10:15 AM")
        )
    )
    val supportTickets = _supportTickets.asStateFlow()

    fun createTicket(orderNumber: String, category: String, description: String) {
        val newId = "SG" + (10000 + (Math.random() * 90000).toInt())
        val newTicket = SupportTicket(
            ticketId = newId,
            orderNumber = orderNumber,
            category = category,
            description = description,
            status = "In Progress",
            createdAt = "Just now"
        )
        _supportTickets.value = listOf(newTicket) + _supportTickets.value
    }
}

// ---------------- ADMIN VIEWMODEL ----------------
class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    val analytics: StateFlow<AdminAnalytics> = adminRepository.analytics.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
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
            cityBreakdown = emptyList(),
            popularDishes = emptyList()
        )
    )
}
