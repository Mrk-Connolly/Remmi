package com.remmi.app.plugins.maps

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.maps.models.SavedLocation

/**
 * Repository for managing [SavedLocation] data via in-memory caching.
 */
class MapsRepository : MemoryRepository<SavedLocation>() {

    init {
        Log.d("Remmi", "[MapsRepository] - Constructor initialized")
    }
}
