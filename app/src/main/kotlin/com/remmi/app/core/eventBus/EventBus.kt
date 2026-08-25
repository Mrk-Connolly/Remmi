package com.remmi.app.core.eventBus

import android.util.Log
import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.CommandOperations
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.EventListener
import com.remmi.app.core.eventBus.events.EventOperations
import com.remmi.app.core.eventBus.events.RemmiEvent

/**
 * EVENT BUS
 *
 * Centralized coordinator for system-wide communication.
 * Delegates actual logic to specific operation handlers for events and commands.
 */
class EventBus {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    private val eventOps = EventOperations()
    private val commandOps = CommandOperations()

    /** Public flows for external observation */
    val events = eventOps.events
    val commands = commandOps.commands


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
    //                                EVENT FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Subscribe Event
     * Register a new listener to receive Fact notifications.
     * */
    fun subscribeEvent(listener: EventListener) {
        eventOps.subscribe(listener)
    }

    /**                                 Unsubscribe Event
     * Remove a previously registered EventListener.
     * */
    fun unsubscribeEvent(listener: EventListener) {
        eventOps.unsubscribe(listener)
    }

    /**                                 Publish Event
     * Distribute a Fact to all subscribed EventListeners.
     * */
    suspend fun publishEvent(event: RemmiEvent) {
        eventOps.publish(event)
    }


    // ----------------------------------------------------------------------------
    //                               COMMAND FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Subscribe Command
     * Register a new listener to receive action requests.
     * */
    fun subscribeCommand(listener: CommandListener) {
        commandOps.subscribe(listener)
    }

    /**                                 Unsubscribe Command
     * Remove a previously registered CommandListener.
     * */
    fun unsubscribeCommand(listener: CommandListener) {
        commandOps.unsubscribe(listener)
    }

    /**                                 Publish Command
     * Distribute an action request to all subscribed CommandListeners.
     * */
    suspend fun publishCommand(command: RemmiCommand) {
        commandOps.publish(command)
    }

    /**                                 Clear
     * Remove all listeners from the bus.
     * */
    fun clear() {
        Log.d("Remmi", "[EventBus] - Clearing all listeners")
        eventOps.clear()
        commandOps.clear()
    }
}
