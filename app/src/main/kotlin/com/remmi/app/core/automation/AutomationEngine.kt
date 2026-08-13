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
     * */

    override fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[AutomationEngine] - [onEvent] executed")
        Log.d("Remmi", "Automation received: ${event::class.simpleName}")
    }


}