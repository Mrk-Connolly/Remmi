package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.events.EventListener
import com.remmi.app.core.events.RemmiEvent

class AutomationEngine : EventListener {

    /**
     * REMMI AUTOMATION ENGINE  will be the head of the AI controller for automatic
     * scanning and creation of events without user interaction.
     *
     * Will be implemented once the core is sturdy needs access to phone services
     *
     * */


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Automation Engine
     * */
    init {
        Log.d("Remmi", "[Automation Engine] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Start automation services
     * */
    fun start(){
        Log.d("Remmi", "[AutomationEngine] - Starting services")
    }

    /**                                 Stop
     * Stop automation services
     * */
    fun stop() {
        Log.d("Remmi", "[AutomationEngine] - Stopping services")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------


    /**                                 On Event
     * Handle events received from the system
     * */
    override fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[AutomationEngine] - [onEvent] executed")
        Log.d("Remmi", "Automation received: ${event::class.simpleName}")
    }

}
