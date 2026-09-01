package com.remmi.app.plugins.weather

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand

/**
 * Action controller for the Weather plugin via EventBus.
 */
class WeatherActions(
    override val id: String = "weather_actions",
    override val name: String = "Weather Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null

    /** Current weather state observed by UI */
    val weatherData = mutableStateOf<WeatherInfo?>(null)
    val isLoading = mutableStateOf(false)

    init {
        Log.d("Remmi", "[WeatherActions] - Constructor initialized")
    }

    /**
     * Request weather data via EventBus.
     */
    suspend fun fetchWeatherData() {
        Log.d("Remmi", "[WeatherActions] - [fetchWeatherData] executed")
        isLoading.value = true
        eventBus?.publishCommand(FetchWeatherCommand())
    }

    /**
     * Update the weather data state (called by plugin when event arrives).
     */
    fun updateWeatherData(info: WeatherInfo) {
        weatherData.value = info
        isLoading.value = false
    }
}
