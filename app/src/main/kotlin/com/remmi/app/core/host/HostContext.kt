package com.remmi.app.core.host

import android.util.Log
import android.content.Context
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.plugins.PluginManager

class HostContext(
    val automationEngine: AutomationEngine,
    val pluginManager: PluginManager,

    // Used to load files and Android APIs
    val androidContext: Context
) {
    init {
        Log.d("Remmi", "[HostContext] - [constructor] executed")
    }
}