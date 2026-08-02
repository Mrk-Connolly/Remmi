package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.events.EventListener
import com.remmi.app.core.events.RemmiEvent

class AutomationEngine : EventListener {

    override fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "Automation received: ${event::class.simpleName}")
    }
}