package com.remmi.app.plugins.maps.repository

import android.util.Log
import com.remmi.app.plugins.maps.models.SavedLocation
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.database.DatabaseService

class MapRepository(databaseService: DatabaseService) : CloudRepository<SavedLocation>(
    databaseService = databaseService,
    tableName = "saved_locations",
    serializer = SavedLocation.serializer()
) {
    init {
        Log.d("Remmi", "[MapRepository] - Constructor initialized")
    }
}
