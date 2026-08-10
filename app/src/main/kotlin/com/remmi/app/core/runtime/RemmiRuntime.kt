package com.remmi.app.core.runtime

import android.content.Context
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.events.EventManager
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.widgets.WidgetManager

class RemmiRuntime (private val androidContext: Context) {

    /**
     *                               REMMI RUNTIME
     *
     * Class called by host to manage the core runtime, has the ability tu load, run and unload
     * all plugins. Automation Engin will be run here and will have access to plugin manager and
     * widget manager
     *
     * */

    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------

    private val automationEngine = AutomationEngine()
    private val pluginManager = PluginManager()
    private val widgetManager = WidgetManager()

    val controller = PluginContext(
        automationEngine = automationEngine,
        pluginManager = pluginManager,
        widgetManager = widgetManager
    )


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   START
     * */
    fun start() {
        // 1. Read plugin list
        pluginManager.readPlugins(androidContext)

        // 2. Load plugins
        pluginManager.loadPlugins(controller)
    }


    /**                                   STOP
     * */

    fun stop() {
        pluginManager.close()
    }



    /**                                    RUN
     * */
    fun run(){
        // Automation engine with event listeners
    }



}
