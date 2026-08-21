package com.remmi.app.plugins.weather

import android.util.Log
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.service.android.WeatherInfo
import com.remmi.app.core.service.android.WeatherService

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
