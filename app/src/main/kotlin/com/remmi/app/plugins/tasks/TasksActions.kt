package com.remmi.app.plugins.tasks

import com.remmi.app.core.actions.RemmiAction

/**
 * Action controller for the Tasks plugin.
 *
 * Manages task-related logic, including creation, completion status, and
 * synchronization with the task database.
 */
class TasksActions(
    private val repository: TasksRepository
) : RemmiAction {
    
    /**
     * Retrieves all tasks currently managed by the plugin.
     */
    suspend fun getAllTasks(): List<TaskItem> {
        return repository.getAll()
    }
    
    /**
     * Synchronizes local tasks with the cloud storage.
     */
    suspend fun sync() {
        repository.sync()
    }
}
