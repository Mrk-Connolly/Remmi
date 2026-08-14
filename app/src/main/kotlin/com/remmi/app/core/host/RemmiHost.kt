package com.remmi.app.core.host

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.runtime.RemmiRuntime

/**
 * Remmi Host
 * Owns the app environment and builds the system
 */
class RemmiHost(val androidContext: Context) {
    
    val runtime = RemmiRuntime(androidContext)
    val automationEngine = AutomationEngine()

    init {
        Log.d("Remmi", "[RemmiHost] - Initialized")
    }

    fun start() {
        Log.d("Remmi", "[RemmiHost] - Starting system")
        runtime.start()
        
        // Register automation engine to events if needed
        runtime.eventManager.registerListener(automationEngine)
    }

    fun stop() {
        Log.d("Remmi", "[RemmiHost] - Stopping system")
        runtime.stop()
    }
}
