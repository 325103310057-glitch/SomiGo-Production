package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CartItem
import com.example.domain.model.Order
import com.example.presentation.components.VegNonVegIndicator
import com.example.presentation.viewmodel.CartViewModel
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.VegGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val selectedTip by viewModel.selectedTip.collectAsStateWithLifecycle()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val deliveryAddress by viewModel.deliveryAddress.collectAsStateWithLifecycle()
    val isGroupOrder by viewModel.isGroupOrder.collectAsStateWithLifecycle()
    val groupCode by viewModel.groupCode.collectAsStateWithLifecycle()
    val friendsItems by viewModel.simulatedFriends.collectAsStateWithLifecycle()
    val useRewardsCoins by viewModel.useRewardsCoins.collectAsStateWithLifecycle()
    val scheduledOption by viewModel.scheduledOption.collectAsStateWithLifecycle()

    var couponInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isPlacingOrder by remember { mutableStateOf(false) }

    val userSubtotal = cartItems.sumOf { it.totalPrice }
    val friendsSubtotal = if (isGroupOrder) friendsItems.sumOf { it.second.sumOf { item -> item.totalPrice } } else 0.0
    val subtotal = userSubtotal + friendsSubtotal

    val deliveryFee = if (subtotal > 0) 25.0 else 0.0
    val taxes = Math.round(subtotal * 0.05 * 10.0) / 10.0
    val platformFee = if (subtotal > 0) 5.0 else 0.0
    val coinsDiscount = if (useRewardsCoins) viewModel.rewardsCoinsDiscount else 0.0
    val totalToPay = Math.max(0.0, subtotal + deliveryFee + taxes + platformFee + selectedTip - discountAmount - coinsDiscount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (isGroupOrder) "SomiGo Group Checkout" else "Checkout & Cart", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        if (isGroupOrder) {
                            Text("Group Code: $groupCode", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty() || (isGroupOrder && friendsItems.isNotEmpty())) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isGroupOrder) "Group Total to Pay" else "Total to Pay",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${totalToPay.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isPlacingOrder) {
                                        isPlacingOrder = true
                                        scope.launch {
                                            viewModel.placeOrder(
                                                restaurantName = "Paradise Dum Biryani",
                                                onOrderPlaced = { order ->
                                                    isPlacingOrder = false
                                                    onOrderPlaced(order)
                                                }
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                                enabled = !isPlacingOrder,
                                modifier = Modifier.testTag("place_order_button")
                            ) {
                                if (isPlacingOrder) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Securing Order...")
                                } else {
                                    Text(if (isGroupOrder) "Place Group Order" else "Pay & Place Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty() && (!isGroupOrder || friendsItems.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your SomiGo Cart is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explore restaurants and pick something fresh!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Browse Restaurants")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 90.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group Order Banner / Switch
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isGroupOrder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isGroupOrder) "Group Order Active" else "Order with Friends?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isGroupOrder) "Code: $groupCode • Bill auto-split" else "Create group link & split delivery automatically",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (!isGroupOrder) {
                                    Button(
                                        onClick = { viewModel.startGroupOrder() },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("start_group_order_btn")
                                    ) {
                                        Text("Start Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.cancelGroupOrder() },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Exit Group", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // If Group Order is active: Show Split Bill Breakdown
                if (isGroupOrder) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CallSplit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Split Bill Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text(
                                    text = "Each person pays for their food + equal share of delivery and taxes.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val splits = viewModel.getSplitBillBreakdown()
                                splits.forEach { person ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(person.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Food: ₹${person.subtotal.toInt()} + Charges: ₹${person.shareOfTaxesFees.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("₹${person.finalTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Ordered Items Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Selected Items",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        VegNonVegIndicator(isVeg = item.isVeg)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "₹${item.unitPrice.toInt()}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Quantity steppers
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.updateQuantity(item, item.quantity - 1) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                text = "${item.quantity}",
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { viewModel.updateQuantity(item, item.quantity + 1) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Delivery Schedule Shortcut
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delivery Timing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val scheduleOptions = listOf("Instant Delivery (~25 mins)", "Today, 8:00 PM", "Tomorrow, 8:30 AM")
                            scheduleOptions.forEach { opt ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { viewModel.setScheduleOption(opt) }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = scheduledOption == opt,
                                        onClick = { viewModel.setScheduleOption(opt) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(opt, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // SomiGo Coins Loyalty & Rewards
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = AmberSecondary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Redeem 100 SomiGo Coins", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Save ₹50 on this order (Balance: 250 Coins)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Checkbox(
                                checked = useRewardsCoins,
                                onCheckedChange = { viewModel.toggleUseRewardsCoins() }
                            )
                        }
                    }
                }

                // Coupon Code Box
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Coupons & Discounts", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VegGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "'$appliedCoupon' applied!",
                                                color = VegGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "You saved ₹${discountAmount.toInt()} with this coupon",
                                                fontSize = 12.sp,
                                                color = VegGreen
                                            )
                                        }
                                        TextButton(onClick = { viewModel.removeCoupon() }) {
                                            Text("Remove", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it },
                                        placeholder = { Text("SOMIGO50 or SOMIGO75") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.applyCoupon(couponInput)
                                            couponInput = ""
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }
                            }
                        }
                    }
                }

                // Delivery Partner Tip
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Delivery Partner Tip",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "100% of your tip goes to the rider.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10.0, 20.0, 30.0, 50.0).forEach { tip ->
                                    val isSelected = selectedTip == tip
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectTip(if (isSelected) 0.0 else tip) },
                                        label = { Text("₹${tip.toInt()}") },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Payment Method
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf("UPI (PhonePe, GPay, Paytm)", "Credit / Debit Card", "Cash on Delivery").forEach { method ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectPaymentMethod(method) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPaymentMethod == method,
                                        onClick = { viewModel.selectPaymentMethod(method) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = method, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Detailed Bill Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Bill Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            BillLineItem("Item Total", "₹${subtotal.toInt()}")
                            BillLineItem("Delivery Partner Fee", "₹${deliveryFee.toInt()}")
                            BillLineItem("Govt. Taxes (5%)", "₹${taxes.toInt()}")
                            BillLineItem("Platform Fee", "₹${platformFee.toInt()}")
                            if (selectedTip > 0) {
                                BillLineItem("Delivery Tip", "₹${selectedTip.toInt()}")
                            }
                            if (discountAmount > 0) {
                                BillLineItem("Coupon Discount", "-₹${discountAmount.toInt()}", isDiscount = true)
                            }
                            if (useRewardsCoins) {
                                BillLineItem("SomiGo Coins Redeemed", "-₹${coinsDiscount.toInt()}", isDiscount = true)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "To Pay", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text(
                                    text = "₹${totalToPay.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BillLineItem(title: String, amount: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDiscount) VegGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}
