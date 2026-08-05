package com.remmi.app.core.plugins

import com.remmi.app.core.actions.ActionManager
import com.remmi.app.core.events.EventManager
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.screens.ScreenManager
import com.remmi.app.core.service.ServiceManager
import com.remmi.app.core.widgets.WidgetManager

class PluginContext(
    val automationEngine:   AutomationEngine,
    val actionManager :     ActionManager,
    val eventManager:       EventManager,
    val pluginManager :     PluginManager,
    val widgetManager :     WidgetManager,
    val serviceManager :    ServiceManager,
    val screenManager :     ScreenManager

)