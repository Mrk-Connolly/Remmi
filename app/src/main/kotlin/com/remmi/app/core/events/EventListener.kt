package com.remmi.app.core.events

/**
 * EVENT LISTENER
 *
 * Interface for components that listen for system notifications (Facts).
 */
fun interface EventListener {

    /**                                 On Event
     * Callback executed when a RemmiEvent is published to the system.
     * */
    suspend fun onEvent(event: RemmiEvent)
}
