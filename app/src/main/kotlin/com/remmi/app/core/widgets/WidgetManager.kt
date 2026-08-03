package com.remmi.app.core.widgets

import android.util.Log
import com.remmi.app.core.plugins.PluginRegistry

class WidgetManager(
    private val pluginRegistry: PluginRegistry
) {

    fun getWidgets(): List<RemmiWidget> {
        Log.d("Remmi", "Function get widgets called")

        return pluginRegistry
            .getPlugins()
            .mapNotNull { it.getWidget() }

    }
}