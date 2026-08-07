package com.remmi.app.plugins.tasks

import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

/**
 * Repository for managing [TaskItem] data.
 *
 * Persists tasks in the "tasks" table of the cloud database and provides
 * in-memory caching.
 */
class TasksRepository(databaseService: DatabaseService) : CloudRepository<TaskItem>(
    databaseService = databaseService,
    tableName = "tasks",
    serializer = TaskItem.serializer()
)
