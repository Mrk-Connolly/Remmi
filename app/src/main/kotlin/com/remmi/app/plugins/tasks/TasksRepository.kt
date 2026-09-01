package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.tasks.models.TaskItem

/**
 * Repository for managing [TaskItem] data.
 *
 * Provides local in-memory caching for tasks.
 */
class TasksRepository : MemoryRepository<TaskItem>() {

    init {
        Log.d("Remmi", "[TasksRepository] - Constructor initialized")
    }
}
