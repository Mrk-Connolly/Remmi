package com.remmi.app.core.runtime

import android.util.Log
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.host.HostContext
import com.remmi.app.core.plugins.PluginManager

class RemmiRuntime (val hostContext: HostContext) {

    init {
        Log.d("Remmi", "[RemmiRuntime] - [constructor] executed")
    }

    /**
     *                               REMMI RUNTIME
     *
     * Class called by host to manage the core runtime, has the ability tu load, run and unload
     * all plugins. Automation Engin will be run here and will have access to plugin manager and
     * widget manager
     *
     * */

    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   START
     * */
    fun start() {
        Log.d("Remmi", "[RemmiRuntime] - [start] executed")
        hostContext.pluginManager.readPlugins(hostContext.androidContext)
        hostContext.pluginManager.loadPlugins()
    }


    /**                                   STOP
     * */

    fun stop() {
        Log.d("Remmi", "[RemmiRuntime] - [stop] executed")
        hostContext.pluginManager.close()
    }

}
