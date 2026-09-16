package com.remmi.app.core.plugin.actions

import com.remmi.app.core.eventBus.EventBus

/**
 * BASE REMMI ACTION
 *
 * Interface for plugin actions that provides a standard 'sync' method.
 */
interface BaseRemmiAction : RemmiAction {
    
    /**
     * Synchronize plugin data with the remote source.
     */
    suspend fun sync()
}
