package com.example.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.CartItem
import com.example.domain.model.MenuItem
import com.example.domain.model.Restaurant
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.VegGreen

@Composable
fun VegNonVegIndicator(isVeg: Boolean, modifier: Modifier = Modifier) {
    val borderColor = if (isVeg) VegGreen else NonVegRed
    val fillColor = if (isVeg) VegGreen else NonVegRed

    Box(
        modifier = modifier
            .size(16.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isVeg) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(fillColor, CircleShape)
            )
        } else {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = fillColor, radius = size.minDimension / 2)
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("restaurant_card_${restaurant.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                // Favorite heart button
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clickable(onClick = onToggleFavorite)
                        .testTag("fav_button_${restaurant.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                // Offer Tag badge
                restaurant.offerTag?.let { offer ->
                    Surface(
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = offer,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Rating Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VegGreen
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${restaurant.rating}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.cuisine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${restaurant.deliveryMinutes} mins",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (restaurant.deliveryFee == 0.0) "FREE DELIVERY" else "₹${restaurant.deliveryFee.toInt()} Delivery",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (restaurant.deliveryFee == 0.0) VegGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(
    item: MenuItem,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VegNonVegIndicator(isVeg = item.isVeg)
                    if (item.discountedPrice != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Bestseller",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${(item.discountedPrice ?: item.price).toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.discountedPrice != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${item.price.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Food image with floating Add/Quantity button
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .align(Alignment.CenterVertically)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(34.dp)
                ) {
                    if (cartQuantity == 0) {
                        Box(
                            modifier = Modifier
                                .clickable(onClick = onAddToCart)
                                .padding(horizontal = 18.dp)
                                .testTag("add_item_${item.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ADD",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = onRemoveFromCart,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                            }
                            Text(
                                text = "$cartQuantity",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = onAddToCart,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTrackingLiveMap(
    order: com.example.domain.model.Order,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Road grid
                val roadColor = Color.White.copy(alpha = 0.65f)
                drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 12f)
                drawLine(roadColor, Offset(0f, h * 0.72f), Offset(w, h * 0.72f), strokeWidth = 12f)
                drawLine(roadColor, Offset(w * 0.22f, 0f), Offset(w * 0.22f, h), strokeWidth = 12f)
                drawLine(roadColor, Offset(w * 0.78f, 0f), Offset(w * 0.78f, h), strokeWidth = 12f)

                val restaurantPoint = Offset(w * 0.22f, h * 0.72f)
                val customerPoint = Offset(w * 0.78f, h * 0.35f)

                // Route between restaurant and customer
                val routePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(restaurantPoint.x, restaurantPoint.y)
                    lineTo(restaurantPoint.x, customerPoint.y)
                    lineTo(customerPoint.x, customerPoint.y)
                }

                drawPath(
                    path = routePath,
                    color = Color(0xFFFF5722).copy(alpha = 0.8f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f),
                        cap = StrokeCap.Round
                    )
                )

                // Restaurant Marker
                drawCircle(color = Color(0xFFFF5722), radius = 14f, center = restaurantPoint)
                drawCircle(color = Color.White, radius = 5f, center = restaurantPoint)

                // Customer Home Marker
                drawCircle(color = Color(0xFF2E7D32), radius = 16f, center = customerPoint)
                drawCircle(color = Color.White, radius = 6f, center = customerPoint)

                // Driver Position based on REAL reported GPS coordinates if present
                if (order.hasLiveGpsSignal && order.deliveryPartnerLatitude != null && order.deliveryPartnerLongitude != null) {
                    val latDelta = (order.deliveryPartnerLatitude - order.restaurantLatitude) / (order.customerLatitude - order.restaurantLatitude).coerceAtLeast(0.0001)
                    val fraction = latDelta.toFloat().coerceIn(0.1f, 0.95f)
                    val driverPos = Offset(
                        x = restaurantPoint.x + (customerPoint.x - restaurantPoint.x) * fraction,
                        y = restaurantPoint.y + (customerPoint.y - restaurantPoint.y) * fraction
                    )

                    drawCircle(
                        color = Color(0xFF2196F3).copy(alpha = pulseAlpha),
                        radius = 26f,
                        center = driverPos
                    )
                    drawCircle(
                        color = Color(0xFF1976D2),
                        radius = 13f,
                        center = driverPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = driverPos
                    )
                }
            }

            // Top Status Badges
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (order.hasLiveGpsSignal) VegGreen else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (order.hasLiveGpsSignal) "LIVE GPS ACTIVE" else "GPS: AWAITING BROADCAST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Dynamic ETA
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 2.dp
                    ) {
                        val etaText = when (order.status) {
                            com.example.domain.model.DeliveryStatus.PLACED -> "Order Received"
                            com.example.domain.model.DeliveryStatus.ACCEPTED -> "Prep in ~20m"
                            com.example.domain.model.DeliveryStatus.PREPARING -> "Kitchen Cooking"
                            com.example.domain.model.DeliveryStatus.READY -> "Ready for Pickup"
                            com.example.domain.model.DeliveryStatus.PICKED_UP -> "Rider Dispatched"
                            com.example.domain.model.DeliveryStatus.ON_THE_WAY -> if (order.hasLiveGpsSignal) "Arriving ~10 mins" else "In Transit"
                            com.example.domain.model.DeliveryStatus.DELIVERED -> "Delivered"
                            com.example.domain.model.DeliveryStatus.CANCELLED -> "Cancelled"
                        }
                        Text(
                            text = etaText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Technical Map API Notice
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Google Maps SDK: NOT CONFIGURED (Requires MAPS_API_KEY). Real GPS Telemetry stream is active.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
