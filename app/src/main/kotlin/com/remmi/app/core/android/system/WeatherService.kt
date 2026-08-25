package com.remmi.app.core.android.system

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
    val currentTemp: Double,
    val temperatureMin: Double,
    val temperatureMax: Int,
    val precipitationProbability: Double,
    val isRainExpected: Boolean,
    val icon: String, // e.g., "sunny", "cloudy", "rainy", "stormy", "clear_night"
    val feelsLike: Double,
    val humidity: Int, // percentage
    val windSpeed: Double, // km/h
    val uvIndex: Int,
    val visibility: Double, // km
    val pressure: Int, // hPa
    val sunrise: String,
    val sunset: String,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList()
)

data class HourlyForecast(
    val time: String, // e.g., "14:00"
    val temp: Double,
    val icon: String
)

data class DailyForecast(
    val day: String, // e.g., "Mon"
    val minTemp: Double,
    val maxTemp: Double,
    val icon: String
)
