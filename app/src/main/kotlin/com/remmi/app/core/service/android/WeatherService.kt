package com.remmi.app.core.service.android

/**
 * WEATHER SERVICE
 *
 * Interface for fetching weather information.
 */
interface WeatherService {

    /**                                 Get Today Weather
     * Retrieve the weather forecast for the current day.
     * */
    suspend fun getTodayWeather(): WeatherInfo
}

/**
 * WEATHER INFO
 *
 * Data model for daily weather summary.
 */
data class WeatherInfo(
    val summary: String,
    val temperatureMin: Double,
    val temperatureMax: Int,
    val precipitationProbability: Double,
    val isRainExpected: Boolean
)
