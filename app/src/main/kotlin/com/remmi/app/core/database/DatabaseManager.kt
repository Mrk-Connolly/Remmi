package com.remmi.app.core.database

import android.util.Log
import com.remmi.app.core.controller.RemmiComponent
import com.remmi.app.core.eventBus.EventBus

/**
 * DATABASE MANAGER
 *
 * Specialized manager for database service lifecycle and configuration.
 * Managers only create and configure their dedicated services.
 */
class DatabaseManager(
    private val eventBus: EventBus,
    private val injectedService: DatabaseService? = null
) : RemmiComponent {

    /** The dedicated database service */
    val service: DatabaseService = injectedService ?: SupabaseService(eventBus)

    init {
        Log.d("Remmi", "[DatabaseManager] - Constructor initialized")
    }

    /**
     * Start the database service.
     */
    override suspend fun start() {
        Log.d("Remmi", "[DatabaseManager] - Starting database service")
        // Subscription is now handled by MemoryService
    }

    /**
     * Stop the database service.
     */
    override fun stop() {
        Log.d("Remmi", "[DatabaseManager] - Stopping database service")
        // Unsubscription is now handled by MemoryService
    }
}
