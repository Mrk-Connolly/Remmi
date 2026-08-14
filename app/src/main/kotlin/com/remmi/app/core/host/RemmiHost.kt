package com.remmi.app.core.host

import android.content.Context
import android.util.Log
import com.remmi.app.core.runtime.RemmiCore

/**
 * Remmi Host
 * Owns the app environment and builds the system
 */
class RemmiHost(val androidContext: Context) {
    
    val runtime = RemmiCore(androidContext)

    init {
        Log.d("Remmi", "[RemmiHost] - Initialized")
    }

    fun start() {
        Log.d("Remmi", "[RemmiHost] - Starting system")
        // Start core functions and load plugins
        runtime.start()
    }

    fun stop() {
        Log.d("Remmi", "[RemmiHost] - Stopping system")
        runtime.stop()
    }
}
