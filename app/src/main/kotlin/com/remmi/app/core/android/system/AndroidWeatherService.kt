package com.remmi.app.core.android.system.implementations

import android.util.Log
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.core.android.system.WeatherService
import com.remmi.app.core.android.system.HourlyForecast
import com.remmi.app.core.android.system.DailyForecast
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.WeatherFetchedEvent

/**
 * ANDROID WEATHER SERVICE
 *
 * Mock implementation of WeatherService.
 */
class AndroidWeatherService(
    private val eventBus: EventBus
) : WeatherService {

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is FetchWeatherCommand -> {
                Log.i("Remmi", "[AndroidWeatherService] - Fetching weather requested")
                val weather = getTodayWeather()
                eventBus.publishEvent(WeatherFetchedEvent(weather))
            }
        }
    }

    override suspend fun getTodayWeather(): WeatherInfo {
        Log.d("Remmi", "[AndroidWeatherService] - Fetching today's weather (Mock)")
        
        // Mock Hourly Forecast
        val hourly = listOf(
            HourlyForecast("13:00", 22.0, "sunny"),
            HourlyForecast("14:00", 23.0, "sunny"),
            HourlyForecast("15:00", 24.5, "cloudy"),
            HourlyForecast("16:00", 24.0, "cloudy"),
            HourlyForecast("17:00", 23.0, "rainy"),
            HourlyForecast("18:00", 21.0, "rainy"),
            HourlyForecast("19:00", 20.0, "cloudy"),
            HourlyForecast("20:00", 19.0, "clear_night"),
            HourlyForecast("21:00", 18.5, "clear_night"),
            HourlyForecast("22:00", 18.0, "clear_night")
        )

        // Mock Daily Forecast
        val daily = listOf(
            DailyForecast("Today", 18.0, 25.0, "sunny"),
            DailyForecast("Tue", 19.0, 26.0, "sunny"),
            DailyForecast("Wed", 20.0, 24.0, "cloudy"),
            DailyForecast("Thu", 17.0, 22.0, "rainy"),
            DailyForecast("Fri", 16.0, 21.0, "stormy"),
            DailyForecast("Sat", 18.0, 24.0, "cloudy"),
            DailyForecast("Sun", 20.0, 27.0, "sunny")
        )

        // Mock data
        return WeatherInfo(
            summary = "Partly cloudy with a chance of showers in the afternoon.",
            currentTemp = 22.5,
            temperatureMin = 18.0,
            temperatureMax = 25,
            precipitationProbability = 0.45,
            isRainExpected = true,
            icon = "cloudy",
            feelsLike = 21.0,
            humidity = 65,
            windSpeed = 12.5,
            uvIndex = 4,
            visibility = 10.0,
            pressure = 1012,
            sunrise = "06:45",
            sunset = "20:15",
            hourlyForecast = hourly,
            dailyForecast = daily
        )
    }
}
