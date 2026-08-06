package com.remmi.app.core.widgets

import android.util.Log
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.RemmiPlugin

class WidgetManager() {

    // Stores all loaded plugins
    private val widgets = mutableMapOf<String, RemmiWidget>()

    fun getWidgets(): List<RemmiWidget> {
        Log.d("Remmi", "Function get widgets called")

        return widgets.mapNotNull { it.value }
    }

    /**
     * Registers a plugin.
     * Returns true if successful, false if a plugin with the same ID already exists.
     */
    fun register(plugin: RemmiPlugin): Boolean {

        Log.d("Remmi", "${plugin.metadata.name} being registered")

        if (!plugin.metadata.showWidget) return false

        if (widgets.containsKey(plugin.metadata.id)) {
            Log.d("Remmi", "${plugin.metadata.name} could not be loaded")
            return false
        }

        widgets[plugin.metadata.id] = plugin.widget

        Log.d("Remmi", "${plugin.metadata.name} loaded correctly")

        return true
    }

    /**
     * Unregisters a plugin.
     */
    fun unregister(id: String): Boolean {

        val plugin = widgets[id] ?: return false
        widgets.remove(id)

        return true
    }
}