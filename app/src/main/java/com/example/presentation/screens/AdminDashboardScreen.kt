package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.presentation.viewmodel.AdminViewModel
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview & Revenue, 1: Geography & Cities, 2: Customer Management

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SomiGo Admin Dashboard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("SomiGo Operations & Fleet Analytics", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Revenue", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Cities", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Customers", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Key Revenue Stats
                item {
                    Text("Revenue Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard("Today", "₹${analytics.todayRevenue.toInt()}", "+14% vs y'day", VegGreen, modifier = Modifier.weight(1f))
                        MetricCard("This Week", "₹${(analytics.weeklyRevenue / 1000).toInt()}K", "98% On-time", MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        MetricCard("This Month", "₹${(analytics.monthlyRevenue / 100000.0).let { String.format("%.2fL", it) }}", "Target met", AmberSecondary, modifier = Modifier.weight(1f))
                    }
                }

                // Order Metrics
                item {
                    Text("Order Volumes & Unit Economics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Orders Placed Today", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${analytics.ordersToday}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly Completed Orders", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${analytics.completedOrders}", fontWeight = FontWeight.Bold, color = VegGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Average Order Value (AOV)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${analytics.averageOrderValue.toInt()}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cancellation Rate", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("1.8% (Target < 2.5%)", fontWeight = FontWeight.Bold, color = VegGreen)
                            }
                        }
                    }
                }

                // Popular Dishes
                item {
                    Text("Top Selling Food Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            analytics.popularDishes.forEachIndexed { index, dish ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(dish.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("${dish.count} orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text("₹${(dish.revenue / 1000).toInt()}K", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Geographic Analytics
                item {
                    Text("City-Level Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aggregated sales and order fulfillment volume across active metropolitan launch hubs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(analytics.cityBreakdown) { city ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(city.city, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VegGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Active Hub",
                                        color = VegGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivered Orders", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${city.orders}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Merchandise Value (GMV)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${(city.revenue / 100000.0).let { String.format("%.2f Lakhs", it) }}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                // Customer Management
                item {
                    Text("Customer Accounts & Audit Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Review registered customers, order frequency, total lifetime spend, and account states.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val customers = listOf(
                    Triple("Kavya Sharma", "+91 98480 54321", "14 Orders • ₹4,820 Spent"),
                    Triple("Ananya Reddy", "+91 97000 65432", "8 Orders • ₹2,940 Spent"),
                    Triple("Vikram Rao", "+91 94401 98765", "22 Orders • ₹8,150 Spent"),
                    Triple("Sneha Patel", "+91 99887 11223", "5 Orders • ₹1,430 Spent")
                )

                items(customers) { (name, phone, stats) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stats, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VegGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = VegGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
private fun MetricCard(title: String, value: String, subtitle: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
        }
    }
}
