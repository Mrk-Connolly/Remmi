package com.remmi.app.core.plugins

import android.content.Context
import android.util.Log
import com.remmi.app.core.widgets.WidgetManager
import com.remmi.app.plugins.calendar.CalendarPlugin
import kotlinx.serialization.json.Json



class PluginManager {

    val plugins = mutableMapOf<String, RemmiPlugin>()
    val pluginMetadata = mutableListOf<PluginMetadata>()

    fun readPlugins(androidContext : Context) {
        Log.d("Remmi", "Accessing plugin information")

        val json = androidContext.assets
            .open("plugins.json")
            .bufferedReader()
            .use { it.readText() }

        pluginMetadata.clear()
        pluginMetadata.addAll(
            Json.decodeFromString<List<PluginMetadata>>(json)
        )
    }

    fun loadPlugins(widgetManager: WidgetManager) {
        Log.d("Remmi", "Loading plugins...")

        plugins.clear()

        pluginMetadata.forEach { metadata ->

            val plugin = when (metadata.id) {
                "calendar" -> CalendarPlugin()

                // Add here additional plugins
                // "tasks" -> TaskPlugin()
                // "weather" -> WeatherPlugin()

                else -> null
            }

            plugin?.let {
                plugins[metadata.id] = plugin
                Log.d("Remmi", "Loaded ${metadata.name}")
            }

            widgetManager.register(CalendarPlugin(), )
        }
    }

    fun close() {
        TODO("Not yet implemented")
    }
}