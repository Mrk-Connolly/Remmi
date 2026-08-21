package com.remmi.app.core.service.android.implementations

import android.util.Log
import com.remmi.app.core.service.android.WeatherInfo
import com.remmi.app.core.service.android.WeatherService

/**
 * ANDROID WEATHER SERVICE
 *
 * Mock implementation of WeatherService.
 */
class AndroidWeatherService : WeatherService {

    override suspend fun getTodayWeather(): WeatherInfo {
        Log.d("Remmi", "[AndroidWeatherService] - Fetching today's weather (Mock)")
        
        // Mock data
        return WeatherInfo(
            summary = "Partly cloudy with a chance of showers.",
            temperatureMin = 18.0,
            temperatureMax = 25,
            precipitationProbability = 0.45,
            isRainExpected = true
        )
    }
}
