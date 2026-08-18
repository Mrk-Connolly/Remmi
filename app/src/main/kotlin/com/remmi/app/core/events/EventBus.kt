package com.remmi.app.core.events

import android.util.Log

/**
 * EVENT BUS
 *
 * Centralized communication channel for system-wide events and commands.
 * Distinguishes between completed facts (Events) and requests for action (Commands).
 */
class EventBus {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Listeners for Facts (something already happened) */
    private val eventListeners = mutableSetOf<EventListener>()

    /** Listeners for Intents (requests to perform an action) */
    private val commandListeners = mutableSetOf<CommandListener>()


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

    /**                                 Subscribe Event
     * Register a new listener to receive Fact notifications.
     * */
    fun subscribeEvent(listener: EventListener) {
        Log.d("Remmi", "[EventBus] - Subscribing new EventListener")
        eventListeners.add(listener)
    }

    /**                                 Unsubscribe Event
     * Remove a previously registered EventListener.
     * */
    fun unsubscribeEvent(listener: EventListener) {
        Log.d("Remmi", "[EventBus] - Unsubscribing EventListener")
        eventListeners.remove(listener)
    }

    /**                                 Subscribe Command
     * Register a new listener to receive action requests.
     * */
    fun subscribeCommand(listener: CommandListener) {
        Log.d("Remmi", "[EventBus] - Subscribing new CommandListener")
        commandListeners.add(listener)
    }

    /**                                 Unsubscribe Command
     * Remove a previously registered CommandListener.
     * */
    fun unsubscribeCommand(listener: CommandListener) {
        Log.d("Remmi", "[EventBus] - Unsubscribing CommandListener")
        commandListeners.remove(listener)
    }

    /**                                 Publish Event
     * Distribute a Fact to all subscribed EventListeners.
     * */
    suspend fun publishEvent(event: RemmiEvent) {
        Log.i("Remmi", "[EventBus] - EVENT PUBLISHED: [${event.type}] from [${event.source}] (ID: ${event.eventId})")
        
        eventListeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e("Remmi", "[EventBus] - EventListener failure for [${event.type}]: ${e.message}")
            }
        }
    }

    /**                                 Publish Command
     * Distribute an action request to all subscribed CommandListeners.
     * */
    suspend fun publishCommand(command: RemmiCommand) {
        Log.i("Remmi", "[EventBus] - COMMAND PUBLISHED: [${command::class.simpleName}] from [${command.source}] (ID: ${command.commandId})")
        
        commandListeners.forEach { listener ->
            try {
                listener.onCommand(command)
            } catch (e: Exception) {
                Log.e("Remmi", "[EventBus] - CommandListener failure for [${command::class.simpleName}]: ${e.message}")
            }
        }
    }

    /**                                 Clear
     * Remove all listeners from the bus.
     * */
    fun clear() {
        Log.d("Remmi", "[EventBus] - Clearing all listeners")
        eventListeners.clear()
        commandListeners.clear()
    }
}
