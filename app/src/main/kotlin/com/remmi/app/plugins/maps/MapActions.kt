package com.remmi.app.plugins.maps

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.PickLocationCommand
import com.remmi.app.core.eventBus.events.LocationPickedEvent
import com.remmi.app.plugins.maps.models.SavedLocation
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.maps.repository.MapRepository
import com.remmi.app.core.controller.GlobalUIState
import kotlinx.datetime.Instant
import java.util.UUID

class MapActions(
    private val repository: MapRepository,
    override val id: String = "map_actions",
    override val name: String = "Map Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null
    
    // Simple cache for geocoded addresses (Name/Address -> LatLng)
    private val geocodeCache = mutableMapOf<String, Pair<Double, Double>>()

    init {
        Log.d("Remmi", "[MapActions] - Constructor initialized")
    }

    suspend fun saveLocation(name: String, address: String?, lat: Double?, lon: Double?, category: String = "General"): SavedLocation {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val loc = SavedLocation(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            name = name,
            address = address,
            latitude = lat,
            longitude = lon,
            category = category
        )
        repository.insert(loc)
        return loc
    }

    suspend fun getAllSavedLocations(): List<SavedLocation> {
        return repository.getAll()
    }

    fun handlePickLocation(command: PickLocationCommand) {
        Log.d("Remmi", "[MapActions] - Handling PickLocationCommand: ${command.requestId}")
        GlobalUIState.locationPickerRequestId.value = command.requestId
        GlobalUIState.locationPickerInitialSearch.value = command.initialSearch
        GlobalUIState.showLocationPicker.value = true
    }

    suspend fun notifyLocationPicked(requestId: String, name: String, address: String?, lat: Double?, lon: Double?) {
        Log.i("Remmi", "[MapActions] - Publishing LocationPickedEvent for request: $requestId")
        eventBus?.publishEvent(
            LocationPickedEvent(
                requestId = requestId,
                name = name,
                address = address,
                latitude = lat,
                longitude = lon
            )
        )
        
        // Cache result if valid
        if (lat != null && lon != null) {
            geocodeCache[name] = Pair(lat, lon)
            address?.let { geocodeCache[it] = Pair(lat, lon) }
        }
    }

    fun getCachedLocation(query: String): Pair<Double, Double>? {
        return geocodeCache[query]
    }

    suspend fun sync() {
        repository.sync()
    }
}
