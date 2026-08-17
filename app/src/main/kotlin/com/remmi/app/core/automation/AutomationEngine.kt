package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.events.*

/**
 * AUTOMATION ENGINE
 *
 * Central intelligence of the system that reacts to events (Facts) and issues commands (Intents).
 * Coordinates cross-plugin operations without direct dependencies between plugins.
 */
class AutomationEngine(
    private val eventBus: EventBus
) : EventListener {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Flag indicating if the engine is currently running and listening to events */
    private var running = false


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[AutomationEngine] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Subscribe to Fact events on the EventBus.
     * */
    fun start() {
        if (running) return
        Log.d("Remmi", "[AutomationEngine] - Starting automation services")
        eventBus.subscribeEvent(this)
        running = true
    }

    /**                                 Stop
     * Unsubscribe from the Fact channel.
     * */
    fun stop() {
        if (!running) return
        Log.d("Remmi", "[AutomationEngine] - Stopping automation services")
        eventBus.unsubscribeEvent(this)
        running = false
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Event
     * Handle Facts distributed via the EventBus.
     * Decisions made here will result in Commands being published.
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.i("Remmi", "[AutomationEngine] - RECEIVED FACT: [${event.type}] from [${event.source}]")
        
        when (event) {
            is PluginEvent -> handlePluginEvent(event)
        }
    }

    /**                                 Handle Plugin Event
     * Logic for cross-plugin automation based on standard CRUD facts.
     * */
    private suspend fun handlePluginEvent(event: PluginEvent) {
        // EXAMPLE: Linked resource cleanup
        if (event.source == "calendar" && event.type == EventType.DELETED) {
            Log.i("Remmi", "[AutomationEngine] - Calendar event deleted. Checking for linked Alarms...")
            
            // TODO: In a real implementation, we would lookup linkedAlarmId from a mapping service/db
            val linkedAlarmId: String? = null 
            
            linkedAlarmId?.let { alarmId ->
                Log.i("Remmi", "[AutomationEngine] - Issuing DeleteAlarmCommand for: $alarmId")
                eventBus.publishCommand(
                    DeleteAlarmCommand(alarmId = alarmId)
                )
            }
        }
    }
}
