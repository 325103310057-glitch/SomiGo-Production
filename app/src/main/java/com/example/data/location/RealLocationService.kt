package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestamp: Long,
    val isMock: Boolean = false,
    val resolvedAddress: String = "",
    val city: String = "Visakhapatnam",
    val postalCode: String = ""
)

sealed class LocationResult {
    data class Success(val location: DeviceLocation) : LocationResult()
    data class Error(val type: LocationErrorType, val message: String) : LocationResult()
}

enum class LocationErrorType {
    PERMISSION_DENIED,
    GPS_DISABLED,
    UNAVAILABLE,
    TIMEOUT
}

class RealLocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onResult: (LocationResult) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onResult(
                LocationResult.Error(
                    LocationErrorType.PERMISSION_DENIED,
                    "Location permission is not granted. Please allow location access to discover nearby restaurants and receive accurate delivery."
                )
            )
            return
        }

        if (!isGpsEnabled()) {
            onResult(
                LocationResult.Error(
                    LocationErrorType.GPS_DISABLED,
                    "Device GPS is turned off. Please turn on Location services in Settings."
                )
            )
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        location.isMock
                    } else {
                        @Suppress("DEPRECATION")
                        location.isFromMockProvider
                    }

                    val geocodeResult = reverseGeocodeCoordinates(location.latitude, location.longitude)
                    val deviceLocation = DeviceLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        timestamp = location.time,
                        isMock = isMock,
                        resolvedAddress = geocodeResult.first,
                        city = geocodeResult.second,
                        postalCode = geocodeResult.third
                    )
                    onResult(LocationResult.Success(deviceLocation))
                } else {
                    // Fallback to last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            val geocode = reverseGeocodeCoordinates(lastLoc.latitude, lastLoc.longitude)
                            onResult(
                                LocationResult.Success(
                                    DeviceLocation(
                                        latitude = lastLoc.latitude,
                                        longitude = lastLoc.longitude,
                                        accuracyMeters = lastLoc.accuracy,
                                        timestamp = lastLoc.time,
                                        resolvedAddress = geocode.first,
                                        city = geocode.second,
                                        postalCode = geocode.third
                                    )
                                )
                            )
                        } else {
                            onResult(
                                LocationResult.Error(
                                    LocationErrorType.UNAVAILABLE,
                                    "Unable to acquire GPS fix. Please ensure you are not indoors or set your delivery address manually."
                                )
                            )
                        }
                    }.addOnFailureListener {
                        onResult(
                            LocationResult.Error(
                                LocationErrorType.UNAVAILABLE,
                                "Location acquisition failed: ${it.localizedMessage}"
                            )
                        )
                    }
                }
            }.addOnFailureListener { e ->
                onResult(
                    LocationResult.Error(
                        LocationErrorType.UNAVAILABLE,
                        "GPS error: ${e.localizedMessage}"
                    )
                )
            }
        } catch (e: SecurityException) {
            onResult(
                LocationResult.Error(
                    LocationErrorType.PERMISSION_DENIED,
                    "Security exception: ${e.localizedMessage}"
                )
            )
        } catch (e: Exception) {
            onResult(
                LocationResult.Error(
                    LocationErrorType.UNAVAILABLE,
                    "Unexpected location error: ${e.localizedMessage}"
                )
            )
        }
    }

    fun reverseGeocodeCoordinates(latitude: Double, longitude: Double): Triple<String, String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val line = addr.getAddressLine(0) ?: "${addr.subLocality ?: ""}, ${addr.locality ?: ""}".trim(',', ' ')
                val city = addr.locality ?: addr.subAdminArea ?: "Visakhapatnam"
                val postal = addr.postalCode ?: ""
                Triple(line.ifBlank { "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}" }, city, postal)
            } else {
                Triple("Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}", "Visakhapatnam", "")
            }
        } catch (e: Exception) {
            Triple("Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}", "Visakhapatnam", "")
        }
    }
}
