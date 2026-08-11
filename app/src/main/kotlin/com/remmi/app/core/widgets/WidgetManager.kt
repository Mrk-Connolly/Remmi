package com.remmi.app.core.widgets

import android.util.Log
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.RemmiPlugin

class WidgetManager() {

    /** Widget Manager class hold the operations to load, run and maintain all
     * plugin widgets.
     */




    // ----------------------------------------------------------------------------
    //                                 Variables
    // ----------------------------------------------------------------------------


    /** Mutable map of Remmi Widgets*/
    private val widgets = mutableMapOf<String, RemmiWidget>()





    // ----------------------------------------------------------------------------
    //                                 Core operations
    // ----------------------------------------------------------------------------


    /**                                 REGISTER
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

    /**                                UNREGISTER
     * Unregisters a plugin.
     */
    fun unregister(id: String): Boolean {

        val plugin = widgets[id] ?: return false
        widgets.remove(id)

        return true
    }

    fun clear() {
        widgets.clear()
    }





    // ----------------------------------------------------------------------------
    //                                 Getters
    // ----------------------------------------------------------------------------


    fun getWidgets(allowedIds: Set<String>): List<Pair<String, RemmiWidget>> {
        Log.d("Remmi", "Function get widgets called")

        return widgets.filter { it.key in allowedIds }.map { it.key to it.value }
    }

}