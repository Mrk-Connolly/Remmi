package com.remmi.app.plugins.alarm

import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

/**
 * Repository for managing [AlarmItem] data.
 *
 * Persists alarms in the cloud and provides local caching.
 */
class AlarmRepository(databaseService: DatabaseService) : CloudRepository<AlarmItem>(
    databaseService = databaseService,
    tableName = "alarms",
    serializer = AlarmItem.serializer()
)
