package com.remmi.app.plugins.weather.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerialName("current") val current: CurrentWeather? = null,
    @SerialName("hourly") val hourly: HourlyData? = null,
    @SerialName("daily") val daily: DailyData? = null
)

@Serializable
data class CurrentWeather(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("apparent_temperature") val feelsLike: Double,
    val precipitation: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("wind_direction_10m") val windDirection: Int
)

@Serializable
data class HourlyData(
    val time: List<String>,
    @SerialName("temperature_2m") val temperatures: List<Double>,
    @SerialName("apparent_temperature") val feelsLike: List<Double>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int>,
    val precipitation: List<Double>,
    @SerialName("weather_code") val weatherCodes: List<Int>,
    @SerialName("wind_speed_10m") val windSpeeds: List<Double>
)

@Serializable
data class DailyData(
    val time: List<String>,
    @SerialName("weather_code") val weatherCodes: List<Int>,
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>,
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double>
)

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@Serializable
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class WindSpeedUnit { KMH, MPH }
enum class LocationMode { DEVICE, MANUAL }

@Serializable
data class WeatherSettings(
    val locationMode: LocationMode = LocationMode.DEVICE,
    val manualLatitude: Double? = null,
    val manualLongitude: Double? = null,
    val manualCityName: String? = null,
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val lastUpdate: Long = 0
)
