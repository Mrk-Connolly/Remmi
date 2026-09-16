package com.remmi.app.core.controller

/**
 * Interface for system-level components that have a managed lifecycle.
 */
interface RemmiComponent {
    /** Initialize and start the component. */
    suspend fun start()
    /** Stop and release resources. */
    fun stop()
}
