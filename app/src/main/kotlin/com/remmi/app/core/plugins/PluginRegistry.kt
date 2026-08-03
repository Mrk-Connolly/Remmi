package com.remmi.app.core.plugins

import android.util.Log

class PluginRegistry {

    // Stores all loaded plugins
    private val plugins = mutableMapOf<String, RemmiPlugin>()

    /**
     * Registers a plugin.
     * Returns true if successful, false if a plugin with the same ID already exists.
     */
    fun register(plugin: RemmiPlugin, context: PluginContext): Boolean {

        Log.d("Remmi", "${plugin.metadata.name} being registered")

        if (plugins.containsKey(plugin.metadata.id)) {
            Log.d("Remmi", "${plugin.metadata.name} could not be loaded")
            return false
        }


        plugins[plugin.metadata.id] = plugin
        plugin.onLoad(context)
        Log.d("Remmi", "${plugin.metadata.name} loaded correctly")

        return true
    }

    /**
     * Unregisters a plugin.
     */
    fun unregister(id: String): Boolean {

        val plugin = plugins[id] ?: return false

        plugin.onUnload()
        plugins.remove(id)

        return true
    }

    /**
     * Returns a plugin by ID.
     */
    fun getPlugin(id: String): RemmiPlugin? {
        return plugins[id]
    }

    /**
     * Returns every registered plugin.
     */
    fun getPlugins(): List<RemmiPlugin> {
        return plugins.values.toList()
    }

    /**
     * Checks if a plugin is registered.
     */
    fun isRegistered(id: String): Boolean {
        return plugins.containsKey(id)
    }

    /**
     * Returns the number of loaded plugins.
     */
    fun pluginCount(): Int {
        return plugins.size
    }
}