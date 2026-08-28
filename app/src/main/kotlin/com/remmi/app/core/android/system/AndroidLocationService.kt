package com.remmi.app.core.android.system.implementations

import android.util.Log
import com.remmi.app.core.android.system.LocationService

/**
 * ANDROID LOCATION SERVICE
 *
 * Mock implementation of LocationService for startup.
 */
class AndroidLocationService : LocationService {

    override fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit) {
        Log.d("Remmi", "[AndroidLocationService] - Requesting current location (Mock)")
        // Default to Paris, France for mock
        onLocationResult(48.8566, 2.3522)
    }
}
