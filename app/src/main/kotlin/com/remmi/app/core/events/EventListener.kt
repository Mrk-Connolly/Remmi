package com.remmi.app.core.events

/**
 * Interface for components that listen for system events.
 */
fun interface EventListener {

    /**                                 On Event
     * Callback executed when a RemmiEvent is published to the system.
     * Must be a suspend function to allow asynchronous processing.
     * */
    suspend fun onEvent(event: RemmiEvent)
}
