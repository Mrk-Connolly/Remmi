package com.remmi.app.plugins.maps

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.events.LocationPickedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.maps.models.SavedLocation
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Actions for the Maps plugin.
 */
class MapsActions(
    private val repository: MapsRepository,
    override val id: String = "maps_actions",
    override val name: String = "Maps Actions"
) : RemmiAction {
    override var eventBus: EventBus? = null
    
    suspend fun getAllSavedLocations(): List<SavedLocation> {
        return repository.getAll()
    }
    
    suspend fun saveLocation(
        name: String, 
        address: String?, 
        lat: Double?, 
        lon: Double?, 
        linkedCalendarEvent: String? = null
    ): SavedLocation {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val loc = SavedLocation(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            name = name,
            address = address,
            latitude = lat,
            longitude = lon,
            linkedCalendarEvent = linkedCalendarEvent
        )
        repository.add(loc)
        return loc
    }
    
    suspend fun deleteLocation(id: String) {
        repository.remove(id)
    }

    suspend fun notifyLocationPicked(requestId: String, name: String, address: String?, lat: Double?, lon: Double?) {
        Log.i("Remmi", "[MapsActions] - Publishing LocationPickedEvent for request: $requestId")
        eventBus?.publishEvent(
            LocationPickedEvent(
                requestId = requestId,
                name = name,
                address = address,
                latitude = lat,
                longitude = lon
            )
        )
    }

    suspend fun sync() {
        repository.sync()
    }
}
