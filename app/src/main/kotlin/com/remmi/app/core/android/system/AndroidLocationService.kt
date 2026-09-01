package com.remmi.app.core.android.system.implementations

import android.util.Log
import com.remmi.app.core.android.system.LocationService
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.RequestLocationCommand
import com.remmi.app.core.eventBus.events.CurrentLocationRespondedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ANDROID LOCATION SERVICE
 *
 * Mock implementation of LocationService for startup.
 */
class AndroidLocationService(
    private val eventBus: EventBus
) : LocationService {

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is RequestLocationCommand -> {
                Log.i("Remmi", "[AndroidLocationService] - Location requested")
                requestCurrentLocation { lat, lon ->
                    CoroutineScope(Dispatchers.IO).launch {
                        eventBus.publishEvent(CurrentLocationRespondedEvent(lat, lon))
                    }
                }
            }
        }
    }

    override fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit) {
        Log.d("Remmi", "[AndroidLocationService] - Requesting current location (Mock)")
        // Default to Paris, France for mock
        onLocationResult(48.8566, 2.3522)
    }
}
