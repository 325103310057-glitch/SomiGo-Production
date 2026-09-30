package com.example.presentation.components

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.DeviceLocation
import com.example.data.location.LocationErrorType
import com.example.data.location.LocationResult
import com.example.data.location.RealLocationService
import com.example.ui.theme.VegGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialCity: String,
    initialAddress: String,
    onDismiss: () -> Unit,
    onLocationConfirmed: (address: String, city: String, lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { RealLocationService(context) }

    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var acquiredLocation by remember { mutableStateOf<DeviceLocation?>(null) }

    var selectedCity by remember { mutableStateOf(initialCity) }
    var addressLine by remember { mutableStateOf(initialAddress) }
    var landmark by remember { mutableStateOf("") }
    var addressTag by remember { mutableStateOf("Home") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLoadingLocation = true
            locationError = null
            locationService.fetchCurrentLocation { result ->
                isLoadingLocation = false
                when (result) {
                    is LocationResult.Success -> {
                        acquiredLocation = result.location
                        selectedCity = result.location.city.ifBlank { selectedCity }
                        addressLine = result.location.resolvedAddress
                    }
                    is LocationResult.Error -> {
                        locationError = result.message
                    }
                }
            }
        } else {
            locationError = "Location permission was denied. You can manually enter your address below."
        }
    }

    fun requestGpsLocation() {
        if (!locationService.hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!locationService.isGpsEnabled()) {
            locationError = "Device GPS is currently turned off. Please turn on Location in system settings."
        } else {
            isLoadingLocation = true
            locationError = null
            locationService.fetchCurrentLocation { result ->
                isLoadingLocation = false
                when (result) {
                    is LocationResult.Success -> {
                        acquiredLocation = result.location
                        selectedCity = result.location.city.ifBlank { selectedCity }
                        addressLine = result.location.resolvedAddress
                    }
                    is LocationResult.Error -> {
                        locationError = result.message
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Delivery Location", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // GPS Acquisition Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Device GPS Location",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (isLoadingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { requestGpsLocation() },
                            modifier = Modifier.fillMaxWidth().testTag("use_gps_location_btn"),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoadingLocation
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Current GPS Location")
                        }

                        // Acquired coordinates badge
                        acquiredLocation?.let { loc ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VegGreen.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Real GPS Fix Acquired (Accuracy: ±${"%.1f".format(loc.accuracyMeters)}m)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VegGreen
                                        )
                                    }
                                    Text(
                                        text = "Lat: ${"%.5f".format(loc.latitude)}, Lng: ${"%.5f".format(loc.longitude)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Error Banner
                        locationError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = err,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = {
                                            try {
                                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Open Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Delivery City Selector
                Text("Delivery City:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val cities = listOf("Visakhapatnam", "Hyderabad", "Vijayawada", "Bengaluru")
                    cities.forEach { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick = { selectedCity = city },
                            label = { Text(city, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Detailed Street & Building Address
                OutlinedTextField(
                    value = addressLine,
                    onValueChange = { addressLine = it },
                    label = { Text("Flat / House No. / Building / Street") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("address_line_input")
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Address Tag (Home / Work / Other)
                Text("Save As:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Work", "Other").forEach { tag ->
                        FilterChip(
                            selected = addressTag == tag,
                            onClick = { addressTag = tag },
                            label = { Text(tag, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = acquiredLocation?.latitude ?: 17.6868
                    val lng = acquiredLocation?.longitude ?: 83.2185
                    val finalAddr = if (landmark.isNotBlank()) "$addressLine (Near $landmark)" else addressLine
                    onLocationConfirmed(finalAddr.ifBlank { "Beach Road, Visakhapatnam" }, selectedCity, lat, lng)
                },
                modifier = Modifier.testTag("confirm_delivery_location_btn")
            ) {
                Text("Confirm Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
