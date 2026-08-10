package com.remmi.app.core.plugins

import com.remmi.app.core.events.EventManager
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.widgets.WidgetManager

class PluginContext(
    val automationEngine:   AutomationEngine,
    val pluginManager :     PluginManager,
    val widgetManager :     WidgetManager
)
