package com.remmi.app.plugins.gift

import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

class GiftRepository(databaseService: DatabaseService) : CloudRepository<GiftIdea>(
    databaseService = databaseService,
    tableName = "gift_ideas",
    serializer = GiftIdea.serializer()
)
