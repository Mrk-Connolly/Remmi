package com.remmi.app.plugins.weather

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.plugins.weather.ui.screens.WeatherScreen

class WeatherPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {

    private var _actions: WeatherActions? = null

    override val repository: com.remmi.app.core.plugin.repository.RemmiRepository<*>
        get() = throw UnsupportedOperationException("Weather plugin does not use a repository")

    override val actions: WeatherActions
        get() = _actions ?: throw IllegalStateException("WeatherPlugin not initialized")

    override val widget: RemmiWidget by lazy { WeatherWidget(metadata, actions) }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            WeatherScreen(actions, controller)
        }
    }

    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[WeatherPlugin] - Initializing")
        _actions = WeatherActions(context.androidManager.weatherService).apply {
            this.eventBus = context.eventBus
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[WeatherPlugin] - [onCommand] executed")
    }

    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[WeatherPlugin] - [onEvent] executed")
    }

    override fun onLoad() {
        Log.d("Remmi", "[WeatherPlugin] - [onLoad] executed")
    }

    override suspend fun refresh() {
        Log.d("Remmi", "[WeatherPlugin] - Refreshing data (No-op)")
    }

    override fun onUnload() {
        Log.d("Remmi", "[WeatherPlugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        // No-op
    }
}
