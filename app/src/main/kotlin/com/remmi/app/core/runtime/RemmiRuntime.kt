package com.remmi.app.core.runtime

import android.content.Context
import com.remmi.app.core.actions.ActionManager
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.events.EventManager
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.screens.ScreenManager
import com.remmi.app.core.service.ServiceManager
import com.remmi.app.core.widgets.WidgetManager

class RemmiRuntime (private val androidContext: Context) {

    private val eventManager = EventManager()
    private val automationEngine = AutomationEngine()
    private val pluginManager = PluginManager()
    private val widgetManager = WidgetManager()
    private val screenManager = ScreenManager()
    private val serviceManager = ServiceManager()

    private val actionManager = ActionManager()

    val controller = PluginContext(
        automationEngine = automationEngine,
        actionManager = actionManager,
        eventManager = eventManager,
        pluginManager = pluginManager,
        widgetManager = widgetManager,
        serviceManager = serviceManager,
        screenManager = screenManager
    )


    fun start() {
        // Test db connection
        serviceManager.testDBConnection()

        // 1. Read plugin list
        pluginManager.readPlugins(androidContext)

        // 2. Load plugins
        pluginManager.loadPlugins(widgetManager)

        // 3. Load plugins items
        serviceManager.loadPluginItems(pluginManager)
    }

    fun stop() {
        serviceManager.close()
        pluginManager.close()
    }
}

