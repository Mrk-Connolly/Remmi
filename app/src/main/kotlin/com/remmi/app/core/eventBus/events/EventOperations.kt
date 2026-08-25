package com.remmi.app.core.eventBus.events

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * EVENT OPERATIONS
 *
 * Handles the subscription and distribution of events (completed facts).
 */
class EventOperations {

    private val eventListeners = mutableSetOf<EventListener>()

    private val _events = MutableSharedFlow<RemmiEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    /**
     * Register a new listener to receive Fact notifications.
     */
    fun subscribe(listener: EventListener) {
        Log.d("Remmi", "[EventOperations] - Subscribing new EventListener")
        eventListeners.add(listener)
    }

    /**
     * Remove a previously registered EventListener.
     */
    fun unsubscribe(listener: EventListener) {
        Log.d("Remmi", "[EventOperations] - Unsubscribing EventListener")
        eventListeners.remove(listener)
    }

    /**
     * Distribute a Fact to all subscribed EventListeners.
     */
    suspend fun publish(event: RemmiEvent) {
        Log.i("Remmi", "[EventOperations] - EVENT PUBLISHED: [${event.type}] from [${event.source}] (ID: ${event.eventId})")
        
        _events.emit(event)
        
        eventListeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e("Remmi", "[EventBus] - EventListener failure for [${event.type}]: ${e.message}")
            }
        }
    }

    /**
     * Clear all event listeners.
     */
    fun clear() {
        eventListeners.clear()
    }
}
