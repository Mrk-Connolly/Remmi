package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.plugins.PluginManager

/**
 * AUTOMATION ENGINE
 *
 * Central intelligence of the system that reacts to events and triggers automated actions.
 * Listens to the EventBus and coordinates cross-plugin operations.
 */
class AutomationEngine(
    private val pluginManager: PluginManager,
    private val eventBus: EventBus
) : EventListener {

    /**
     * REMMI AUTOMATION ENGINE  will be the head of the AI controller for automatic
     * scanning and creation of events without user interaction.
     * */


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Flag indicating if the engine is currently running and listening to events */
    private var running = false


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Automation Engine
     * */
    init {
        Log.d("Remmi", "[AutomationEngine] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Subscribe to the EventBus and start processing events
     * */
    fun start(){
        if (running) return
        Log.d("Remmi", "[AutomationEngine] - Starting services")
        eventBus.subscribe(this)
        running = true
    }

    /**                                 Stop
     * Unsubscribe from the EventBus and stop processing
     * */
    fun stop() {
        if (!running) return
        Log.d("Remmi", "[AutomationEngine] - Stopping services")
        eventBus.unsubscribe(this)
        running = false
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------


    /**                                 On Event
     * Entry point for events distributed via the EventBus
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[AutomationEngine] - Received event: ${event.type} from ${event.source}")
        
        when (event) {
            is PluginEvent -> handlePluginEvent(event)
        }
    }

    /**                                 Handle Plugin Event
     * Process standard CRUD events from plugins and determine if automation is needed
     * */
    private suspend fun handlePluginEvent(event: PluginEvent) {
        Log.d("Remmi", "[AutomationEngine] - Processing plugin event for item ${event.itemId}")
        
        // FUTURE: Implement cross-plugin automation logic here
        // Example: If a Calendar event is deleted, find and delete linked Alarms
    }

}
