package com.remmi.app.core.plugins

import com.remmi.app.core.events.EventBus
import com.remmi.app.core.automation.AutomationEngine

class PluginContext(

    val eventBus: EventBus,

    val pluginRegistry: PluginRegistry
)