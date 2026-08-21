package com.remmi.app.testing.core

import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * DATABASE TEST REPOSITORY
 * 
 * Repository for logging test results.
 */
class DatabaseTestRepository(databaseService: DatabaseService) : CloudRepository<DatabaseTestLog>(
    databaseService = databaseService,
    tableName = "database_tests",
    serializer = DatabaseTestLog.serializer()
)
