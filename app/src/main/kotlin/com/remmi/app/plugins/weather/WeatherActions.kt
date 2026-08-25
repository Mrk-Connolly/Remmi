package com.remmi.app.plugins.weather

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.core.android.system.WeatherService

/**
 * Action controller for the Weather plugin.
 */
class WeatherActions(
    private val weatherService: WeatherService,
    override val id: String = "weather_actions",
    override val name: String = "Weather Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null

    init {
        Log.d("Remmi", "[WeatherActions] - Constructor initialized")
    }

    /**
     * Fetch current weather and forecast data.
     */
    suspend fun getWeatherData(): WeatherInfo {
        Log.d("Remmi", "[WeatherActions] - [getWeatherData] executed")
        return weatherService.getTodayWeather()
    }
}
