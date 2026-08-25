package com.remmi.app.plugins.weather

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.eventBus.events.WeatherFetchedEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.plugins.weather.screens.WeatherScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.android.services.AndroidServiceManager

class WeatherPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val androidManager: AndroidServiceManager,
    private val eventBus: EventBus
) : RemmiPlugin {

    // Weather doesn't have a standard repository for models yet.
    override val repository: RemmiRepository<out RemmiModel>
        get() = throw UnsupportedOperationException("Weather plugin has no main repository")

    private val _actions = WeatherActions(androidManager.weatherService).apply {
        this.eventBus = this@WeatherPlugin.eventBus
    }

    override val actions: WeatherActions get() = _actions

    override val widget: RemmiWidget = WeatherWidget(metadata, actions)

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[WeatherPlugin] - [Content] (screen) executed")
            WeatherScreen(actions, controller)
        }
    }

    override suspend fun initialize() {}

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is FetchWeatherCommand -> {
                try {
                    val weather = actions.getWeatherData()
                    eventBus.publishEvent(WeatherFetchedEvent(weather))
                } catch (e: Exception) {
                    Log.e("Remmi", "Failed to fetch weather: ${e.message}")
                }
            }
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {}

    override fun onLoad() {}

    override suspend fun refresh() {}

    override fun onUnload() {}

    override suspend fun reformat() {}
}
