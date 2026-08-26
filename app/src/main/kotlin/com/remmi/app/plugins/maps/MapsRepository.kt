package com.remmi.app.plugins.maps

import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.plugins.maps.models.SavedLocation

/**
 * Repository for managing Map data.
 */
class MapsRepository(databaseService: DatabaseService) : CloudRepository<SavedLocation>(
    databaseService = databaseService,
    tableName = "saved_locations",
    serializer = SavedLocation.serializer()
)
