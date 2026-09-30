package com.example.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.MenuItem
import com.example.domain.model.Restaurant
import com.example.presentation.components.LocationPickerDialog
import com.example.presentation.components.RestaurantCard
import com.example.presentation.components.VegNonVegIndicator
import com.example.presentation.viewmodel.HomeViewModel
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRestaurant: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val restaurants by viewModel.restaurants.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val vegOnly by viewModel.vegOnlyFilter.collectAsStateWithLifecycle()
    val fastDelivery by viewModel.fastDeliveryFilter.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val assistantCriteria by viewModel.assistantCriteria.collectAsStateWithLifecycle()

    var showAssistantDialog by remember { mutableStateOf(false) }
    var showGroupOrderNotice by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val categories = listOf("All", "Biryani", "Pizza", "Burgers", "South Indian", "Chinese", "Desserts")
    val trendingDishes = remember { viewModel.getTrendingDishes() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // SomiGo Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SomiGo",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "FAST",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showLocationPicker = true }.testTag("home_location_picker_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedCity,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Customer Offers quick badge
                    FilledTonalButton(
                        onClick = onNavigateToSearch,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("home_offers_btn")
                    ) {
                        Icon(imageVector = Icons.Default.LocalOffer, contentDescription = "Offers", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Offers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar tap target
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(onClick = onNavigateToSearch)
                        .testTag("home_search_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search in SomiGo: biryani, pizza, dosa...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
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
            // Quick Feature Shortcuts (What Should I Eat, Group Order, Schedule, Rewards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeFeatureShortcut(
                        title = "What to Eat?",
                        subtitle = "Smart Picker",
                        icon = Icons.Default.Psychology,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { showAssistantDialog = true },
                        modifier = Modifier.weight(1f).testTag("what_should_i_eat_btn")
                    )
                    HomeFeatureShortcut(
                        title = "Group Order",
                        subtitle = "Split Bill",
                        icon = Icons.Default.Group,
                        containerColor = AmberSecondary.copy(alpha = 0.15f),
                        iconTint = Color(0xFFD97706),
                        onClick = { showGroupOrderNotice = true },
                        modifier = Modifier.weight(1f).testTag("group_order_shortcut")
                    )
                }
            }

            // Promotional Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.food_hero_banner_1790742736278),
                            contentDescription = "SomiGo Feast Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "SOMIGO EXCLUSIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Craving Good Food?",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Use coupon SOMIGO50 for 50% OFF up to ₹100",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Trending Dishes Horizontal Carousel
            if (trendingDishes.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trending on SomiGo 🔥",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(trendingDishes) { (dish, rest) ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .width(200.dp)
                                        .clickable { onNavigateToRestaurant(rest.id) }
                                ) {
                                    Column {
                                        Box(modifier = Modifier.fillMaxWidth().height(105.dp)) {
                                            AsyncImage(
                                                model = dish.imageUrl,
                                                contentDescription = dish.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                VegNonVegIndicator(isVeg = dish.isVeg, modifier = Modifier.padding(6.dp))
                                            }
                                        }
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = dish.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = rest.name,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "₹${(dish.discountedPrice ?: dish.price).toInt()}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${rest.deliveryMinutes}m",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Food Categories
            item {
                Column {
                    Text(
                        text = "Explore Cuisines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Quick Filters (Pure Veg, Fast Delivery)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vegOnly,
                        onClick = { viewModel.toggleVegFilter() },
                        label = { Text("Pure Veg") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    FilterChip(
                        selected = fastDelivery,
                        onClick = { viewModel.toggleFastDelivery() },
                        label = { Text("Fast (<25 mins)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Section Header
            item {
                Text(
                    text = "Top Restaurants on SomiGo (${restaurants.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Restaurants list
            items(restaurants, key = { it.id }) { restaurant ->
                val isFav = favorites.any { it.restaurantId == restaurant.id }
                RestaurantCard(
                    restaurant = restaurant,
                    isFavorite = isFav,
                    onClick = { onNavigateToRestaurant(restaurant.id) },
                    onToggleFavorite = { viewModel.toggleFavorite(restaurant, isFav) }
                )
            }
        }

        // "What Should I Eat?" Assistant Dialog
        if (showAssistantDialog) {
            AlertDialog(
                onDismissRequest = { showAssistantDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("What Should I Eat?")
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Tell us what you're in the mood for, and SomiGo will find the perfect dish.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Budget Filter
                        Text("Budget", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Pair("Under ₹150", 150.0), Pair("Under ₹250", 250.0), Pair("Any Budget", null)).forEach { (label, value) ->
                                val isSelected = assistantCriteria.budgetMax == value
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateAssistantCriteria(budgetMax = value) },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Diet Preference
                        Text("Diet Preference", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = assistantCriteria.isVegOnly,
                                onClick = { viewModel.updateAssistantCriteria(isVegOnly = !assistantCriteria.isVegOnly) },
                                label = { Text("Vegetarian Only") },
                                leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }

                        // Recommendations Output
                        HorizontalDivider()
                        Text("Matches from Real Menus:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val matches = viewModel.getRecommendedDishes()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matches.take(3).forEach { (dish, rest) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showAssistantDialog = false
                                        onNavigateToRestaurant(rest.id)
                                    }
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        VegNonVegIndicator(isVeg = dish.isVeg)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(dish.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${rest.name} • ₹${(dish.discountedPrice ?: dish.price).toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showAssistantDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Group Order Information Notice Dialog
        if (showGroupOrderNotice) {
            AlertDialog(
                onDismissRequest = { showGroupOrderNotice = false },
                icon = { Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("SomiGo Group Order") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ordering food with coworkers or friends?")
                        Text("1. Browse any restaurant and add dishes.")
                        Text("2. In your Cart, tap 'Start Group Order'.")
                        Text("3. SomiGo automatically splits the bill (item-wise + shared delivery charges) so everyone pays their exact share!")
                    }
                },
                confirmButton = {
                    Button(onClick = { showGroupOrderNotice = false }) {
                        Text("Got It!")
                    }
                }
            )
        }

        // Live Device Location Picker Dialog
        if (showLocationPicker) {
            LocationPickerDialog(
                initialCity = selectedCity,
                initialAddress = "$selectedCity, Andhra Pradesh",
                onDismiss = { showLocationPicker = false },
                onLocationConfirmed = { address, city, lat, lng ->
                    viewModel.setCity(city)
                    showLocationPicker = false
                }
            )
        }
    }
}

@Composable
private fun HomeFeatureShortcut(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
