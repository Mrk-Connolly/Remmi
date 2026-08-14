package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

class GiftRepository(databaseService: DatabaseService) : CloudRepository<GiftIdea>(
    databaseService = databaseService,
    tableName = "gift_ideas",
    serializer = GiftIdea.serializer()
) {
    init {
        Log.d("Remmi", "[GiftRepository] - [constructor] executed")
    }
}
