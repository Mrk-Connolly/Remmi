package com.remmi.app.plugins.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WeatherLocationManager() {

    private fun getClient(context: Context) = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        if (!hasLocationPermission(context)) {
            Log.w("Remmi", "[WeatherLocationManager] - No location permission")
            return null
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = suspendCancellableCoroutine { continuation ->
                getClient(context).getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener { e ->
                    Log.e("Remmi", "[WeatherLocationManager] - Error getting location: ${e.message}")
                    continuation.resume(null)
                }
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
            
            location?.let { it.latitude to it.longitude }
        } catch (e: Exception) {
            Log.e("Remmi", "[WeatherLocationManager] - Exception getting location: ${e.message}")
            null
        }
    }
}
