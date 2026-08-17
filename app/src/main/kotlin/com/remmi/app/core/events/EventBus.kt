package com.remmi.app.core.events

import android.util.Log

/**
 * EVENT BUS
 *
 * Centralized communication channel for system-wide events.
 * Follows the Publish-Subscribe pattern to ensure loose coupling between components.
 */
class EventBus {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Set of currently active listeners */
    private val listeners = mutableSetOf<EventListener>()


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[EventBus] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Start the event bus service.
     * */
    fun start() {
        Log.d("Remmi", "[EventBus] - Starting services")
    }

    /**                                 Stop
     * Stop the event bus and clear all subscriptions.
     * */
    fun stop() {
        Log.d("Remmi", "[EventBus] - Stopping services")
        clear()
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Subscribe
     * Register a new listener to receive events.
     * */
    fun subscribe(listener: EventListener) {
        Log.d("Remmi", "[EventBus] - Subscribing new listener")
        listeners.add(listener)
    }

    /**                                 Unsubscribe
     * Remove a previously registered listener.
     * */
    fun unsubscribe(listener: EventListener) {
        Log.d("Remmi", "[EventBus] - Unsubscribing listener")
        listeners.remove(listener)
    }

    /**                                 Publish
     * Distribute an event to all subscribed listeners.
     * Ensures that one listener failure does not interrupt the delivery to others.
     * */
    suspend fun publish(event: RemmiEvent) {
        Log.d("Remmi", "[EventBus] - Publishing event: ${event.type} from ${event.source}")
        
        listeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e("Remmi", "[EventBus] - Listener failure for event ${event.eventId}: ${e.message}")
            }
        }
    }

    /**                                 Clear
     * Remove all listeners from the bus.
     * */
    fun clear() {
        Log.d("Remmi", "[EventBus] - Clearing all listeners")
        listeners.clear()
    }
}
