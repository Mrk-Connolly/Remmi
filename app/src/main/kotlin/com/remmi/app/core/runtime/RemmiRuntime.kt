package com.remmi.app.core.runtime

import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginRegistry
import com.remmi.app.plugins.calendar.CalendarPlugin

class RemmiRuntime {

    val eventBus = EventBus()
    val automationEngine = AutomationEngine()
    
    val pluginRegistry = PluginRegistry()
    val context = PluginContext(
        eventBus = eventBus,
        pluginRegistry = pluginRegistry
    )
    fun start() {
        pluginRegistry.register(CalendarPlugin(), context)
    }

    fun stop() {
        // Later we'll unload everything
    }
}