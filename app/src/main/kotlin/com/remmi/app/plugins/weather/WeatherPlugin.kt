package com.remmi.app.plugins.weather

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.eventBus.events.WeatherFetchedEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.plugins.weather.ui.screens.WeatherScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.weather.models.LocationMode
import com.remmi.app.plugins.weather.models.TemperatureUnit
import com.remmi.app.plugins.weather.models.WindSpeedUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Weather plugin implementation via EventBus communication.
 */
class WeatherPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {

    // Weather doesn't have a standard repository for models yet.
    override val repository: RemmiRepository<out RemmiModel>
        get() = throw UnsupportedOperationException("Weather plugin has no main repository")

    private val client = OpenMeteoClient()
    private val locationManager = WeatherLocationManager()
    private val weatherRepository = WeatherRepository(client, locationManager)
    private val automation = WeatherAutomation(eventBus)

    private val _actions = WeatherActions(weatherRepository).apply {
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

    override suspend fun initialize() {
        Log.d("Remmi", "[WeatherPlugin] - Initializing components")
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is FetchWeatherCommand -> {
                Log.d("Remmi", "[WeatherPlugin] - Handling FetchWeatherCommand")
                val context = WeatherContext.context ?: return
                val weather = weatherRepository.fetchWeather(context)
                weather?.let {
                    eventBus.publishEvent(WeatherFetchedEvent(it))
                }
            }
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is WeatherFetchedEvent -> {
                Log.d("Remmi", "[WeatherPlugin] - Received weather event. Updating actions state.")
                actions.updateWeatherData(event.weather)
            }
        }
    }

    override fun onLoad() {
        Log.d("Remmi", "[WeatherPlugin] - onLoad")
        
        // Load initial data from cache for immediate display
        WeatherContext.context?.let { ctx ->
            weatherRepository.getCachedWeather(ctx)?.let { cached ->
                Log.d("Remmi", "[WeatherPlugin] - Loaded cached weather on load")
                actions.updateWeatherData(cached)
            }
        }
        
        // Periodic sync is handled by AutomationEngine
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        Log.d("Remmi", "[WeatherPlugin] - Refreshing weather")
        actions.fetchWeatherData()
    }

    override fun onUnload() {
        automation.stop()
    }

    override suspend fun reformat() {}
}
