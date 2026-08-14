package com.remmi.app.core.runtime

import android.content.Context
import android.util.Log
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.events.EventManager
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.service.ServiceManager

/**
 * REMMI RUNTIME
 *
 * Class called by host to manage the core runtime, has the ability to load, run and unload
 * all plugins.
 */
class RemmiRuntime(val androidContext: Context) {

    val pluginManager = PluginManager()
    val serviceManager = ServiceManager(androidContext)
    val eventManager = EventManager()
    val eventBus = EventBus()

    init {
        Log.d("Remmi", "[RemmiRuntime] - Initialized")
    }

    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**
     * START
     */
    fun start() {
        Log.d("Remmi", "[RemmiRuntime] - Starting")
        pluginManager.readPlugins(androidContext)
        pluginManager.loadPlugins()
    }

    /**
     * STOP
     */
    fun stop() {
        Log.d("Remmi", "[RemmiRuntime] - Stopping")
        pluginManager.close()
        serviceManager.stop()
    }
}
