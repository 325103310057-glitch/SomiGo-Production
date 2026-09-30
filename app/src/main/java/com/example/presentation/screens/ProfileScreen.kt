package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.SupportViewModel
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    supportViewModel: SupportViewModel,
    onNavigateToOrders: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customerProfile by authViewModel.customerProfile.collectAsStateWithLifecycle()
    val rewardAccount by supportViewModel.rewardAccount.collectAsStateWithLifecycle()
    val supportTickets by supportViewModel.supportTickets.collectAsStateWithLifecycle()

    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showCouponsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Authenticated User Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // User Avatar with Initials
                            val initials = customerProfile.fullName.trim()
                                .split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                                .ifBlank { "U" }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customerProfile.fullName.ifBlank { "SomiGo Customer" },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = customerProfile.email.ifBlank { "customer@somigo.in" },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = customerProfile.phoneNumber.ifBlank { "+91 98491 23456" },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified Account • Customer ID: #${customerProfile.userId}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(
                                onClick = { showEditProfileDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("edit_profile_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 2. Quick Customer Shortcuts (Orders, Favorites, Addresses)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomerQuickAction(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "My Orders",
                        onClick = onNavigateToOrders,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerQuickAction(
                        icon = Icons.Default.Favorite,
                        label = "Favorites",
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f)
                    )
                    CustomerQuickAction(
                        icon = Icons.Default.Place,
                        label = "Addresses",
                        onClick = { showAddressDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. SomiGo Coins Loyalty Balance
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("rewards_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmberSecondary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("SomiGo Coins Balance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Worth ₹${rewardAccount.rupeeValue.toInt()} on your next orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("${rewardAccount.pointsBalance}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFFD97706))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Recent Coin Activity:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        rewardAccount.recentTransactions.take(2).forEach { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tx.description, fontSize = 12.sp)
                                Text("+${tx.points} Coins", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VegGreen)
                            }
                        }
                    }
                }
            }

            // 4. Food Orders & Offers Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Food Orders & Offers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        CustomerMenuItem(
                            icon = Icons.Default.LocalOffer,
                            title = "Coupons & Offers",
                            subtitle = "View active promo codes",
                            onClick = { showCouponsDialog = true }
                        )
                        CustomerMenuItem(
                            icon = Icons.Default.Payment,
                            title = "Payment Preferences",
                            subtitle = "UPI, Cards, Cash on Delivery",
                            onClick = { showPaymentDialog = true }
                        )
                        CustomerMenuItem(
                            icon = Icons.Default.Place,
                            title = "Manage Saved Addresses",
                            subtitle = "Home, Office, Other locations",
                            onClick = { showAddressDialog = true }
                        )
                    }
                }
            }

            // 5. Active Support Tickets (Customer Help)
            item {
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
                            Text(text = "Help & Support Tickets", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "${supportTickets.size} Tickets", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (supportTickets.isEmpty()) {
                            Text(
                                text = "No active support tickets. We are here to help anytime!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            supportTickets.take(2).forEach { ticket ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = ticket.ticketId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${ticket.category} • ${ticket.orderNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (ticket.status == "Resolved") VegGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = ticket.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.status == "Resolved") VegGreen else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Preferences & Play Store Updates
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "App Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Notification Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Order Notifications", fontSize = 14.sp)
                                    Text(text = "Get real-time food delivery alerts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = pushNotificationsEnabled,
                                onCheckedChange = { pushNotificationsEnabled = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Google Play Update Check
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showUpdateDialog = true }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "SomiGo App Version", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 7. Privacy, Terms & Sign Out / Account Deletion
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Privacy & Security", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        CustomerMenuItem(
                            icon = Icons.Default.Security,
                            title = "Privacy Policy",
                            subtitle = "How SomiGo securely handles customer data",
                            onClick = {}
                        )
                        CustomerMenuItem(
                            icon = Icons.Default.Description,
                            title = "Terms of Service",
                            subtitle = "Ordering and refund policies",
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sign Out Button
                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_logout_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delete Account Button (Google Play Compliance)
                        TextButton(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("delete_account_btn")
                        ) {
                            Text(
                                text = "Delete Account & Personal Data",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // --- Dialogs ---

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            var newName by remember { mutableStateOf(customerProfile.fullName) }
            var newEmail by remember { mutableStateOf(customerProfile.email) }
            var newPhone by remember { mutableStateOf(customerProfile.phoneNumber) }

            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Customer Profile", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_email_input")
                        )
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("edit_phone_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.updateProfile(newName, newEmail, newPhone) {
                                showEditProfileDialog = false
                            }
                        },
                        modifier = Modifier.testTag("save_profile_btn")
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Saved Addresses Dialog
        if (showAddressDialog) {
            AlertDialog(
                onDismissRequest = { showAddressDialog = false },
                title = { Text("Saved Delivery Addresses", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Home", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Flat 402, Sea Breeze Apts, Beach Road, Visakhapatnam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Office", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tech Park Sector 5, MVP Colony, Visakhapatnam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddressDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Payment Preferences Dialog
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = { Text("Payment Preferences", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Default Methods Saved for Faster Checkout:", fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UPI (Google Pay, PhonePe, Paytm)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saved Credit/Debit Cards (Visa/Mastercard)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cash on Delivery (Available for orders under ₹1,000)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPaymentDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Coupons Dialog
        if (showCouponsDialog) {
            AlertDialog(
                onDismissRequest = { showCouponsDialog = false },
                title = { Text("Available Coupons", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SOMIGO50", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text("50% OFF up to ₹100 on orders above ₹199", fontSize = 12.sp)
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = AmberSecondary.copy(alpha = 0.2f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("SOMIGO75", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFFD97706))
                                Text("Flat ₹75 OFF on Biryani & South Indian favorites", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCouponsDialog = false }) {
                        Text("Got it")
                    }
                }
            )
        }

        // Delete Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Account Permanently?") },
                text = {
                    Text(
                        "Are you sure you want to delete your SomiGo account? This will permanently erase your profile, saved addresses, order history, and forfeit all accumulated SomiGo Coins. This action cannot be undone."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountDialog = false
                            authViewModel.deleteAccount {
                                onLogout()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Yes, Delete My Account")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Google Play Update Dialog
        if (showUpdateDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                icon = {
                    Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                title = { Text("SomiGo Update") },
                text = {
                    Text("You are using the latest official build of SomiGo (v${BuildConfig.VERSION_NAME}). Check Google Play for newly released updates.")
                },
                confirmButton = {
                    Button(onClick = {
                        showUpdateDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    }) {
                        Text("Check on Google Play")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CustomerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
