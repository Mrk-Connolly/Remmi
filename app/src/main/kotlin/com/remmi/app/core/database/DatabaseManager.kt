package com.remmi.app.core.database

import android.util.Log
import com.remmi.app.core.eventBus.EventBus

/**
 * DATABASE MANAGER
 *
 * Specialized manager for database service lifecycle and configuration.
 * Managers only create and configure their dedicated services.
 */
class DatabaseManager(
    private val eventBus: EventBus
) {

    /** The dedicated database service */
    val service: DatabaseService = SupabaseService(eventBus)

    init {
        Log.d("Remmi", "[DatabaseManager] - Constructor initialized")
    }

    /**
     * Start the database service.
     */
    fun start() {
        Log.d("Remmi", "[DatabaseManager] - Starting database service")
        eventBus.subscribeCommand(service)
    }

    /**
     * Stop the database service.
     */
    fun stop() {
        Log.d("Remmi", "[DatabaseManager] - Stopping database service")
        eventBus.unsubscribeCommand(service)
    }
}
