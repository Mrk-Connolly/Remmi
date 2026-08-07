package com.remmi.app.plugins.alarm

import com.remmi.app.core.actions.RemmiAction

/**
 * Action controller for the Alarm plugin.
 *
 * Handles alarm scheduling, toggling, and synchronization.
 */
class AlarmActions(
    private val repository: AlarmRepository
) : RemmiAction {
    
    /**
     * Retrieves all alarms.
     */
    suspend fun getAllAlarms(): List<AlarmItem> {
        return repository.getAll()
    }
    
    /**
     * Syncs alarms with cloud storage.
     */
    suspend fun sync() {
        repository.sync()
    }
}
