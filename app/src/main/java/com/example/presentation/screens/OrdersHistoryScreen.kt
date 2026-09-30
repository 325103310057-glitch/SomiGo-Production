package com.example.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DeliveryStatus
import com.example.domain.model.Order
import com.example.presentation.viewmodel.CartViewModel
import com.example.presentation.viewmodel.OrderViewModel
import com.example.presentation.viewmodel.SupportViewModel
import com.example.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    viewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    supportViewModel: SupportViewModel,
    onTrackOrder: (Order) -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.ordersHistory.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()

    var showRatingDialog by remember { mutableStateOf<Order?>(null) }
    var showSupportDialog by remember { mutableStateOf<Order?>(null) }
    var supportCategory by remember { mutableStateOf("Missing Item") }
    var supportDescription by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My SomiGo Orders", fontWeight = FontWeight.Bold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (orders.isEmpty() && activeOrder == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No SomiGo orders yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "Once you place an order, you can track it live here.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Order card if present
                activeOrder?.let { active ->
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "ACTIVE ORDER",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = active.status.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = active.restaurantName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${active.items.size} items • ₹${active.totalAmount.toInt()}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onTrackOrder(active) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("track_active_order_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Track Live on Map (PIN: ${active.deliveryPin})")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Past Orders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(orders, key = { it.id }) { order ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = order.restaurantName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${order.placedAt} • ${order.orderNumber}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VegGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Delivered",
                                        color = VegGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Paid: ₹${order.totalAmount.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Need Help / Ticket
                                    OutlinedButton(
                                        onClick = { showSupportDialog = order },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Help", fontSize = 11.sp)
                                    }

                                    // Rate Food
                                    OutlinedButton(
                                        onClick = { showRatingDialog = order },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Rate", fontSize = 11.sp)
                                    }

                                    // "Order Again" (Reorder)
                                    Button(
                                        onClick = {
                                            viewModel.reorder(order, cartViewModel)
                                            onNavigateToCart()
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.testTag("reorder_btn_${order.id}")
                                    ) {
                                        Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Order Again", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rating Dialog
        showRatingDialog?.let { order ->
            AlertDialog(
                onDismissRequest = { showRatingDialog = null },
                title = { Text("Rate your meal") },
                text = {
                    Column {
                        Text("How was your food from ${order.restaurantName}?")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(32.dp).padding(2.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showRatingDialog = null }) {
                        Text("Submit Review")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRatingDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Support Ticket Creation Dialog
        showSupportDialog?.let { order ->
            AlertDialog(
                onDismissRequest = { showSupportDialog = null },
                title = { Text("Need Help with Order #${order.orderNumber}?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Select Issue Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val issueOptions = listOf("Missing Item", "Wrong Item", "Late Delivery", "Payment / Refund")
                        issueOptions.forEach { opt ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = supportCategory == opt,
                                    onClick = { supportCategory = opt }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(opt, fontSize = 13.sp)
                            }
                        }

                        OutlinedTextField(
                            value = supportDescription,
                            onValueChange = { supportDescription = it },
                            placeholder = { Text("Describe the issue in detail...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        supportViewModel.createTicket(order.orderNumber, supportCategory, supportDescription)
                        showSupportDialog = null
                        supportDescription = ""
                    }) {
                        Text("Generate Ticket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSupportDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
