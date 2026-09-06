package com.remmi.app.plugins.weather

import android.content.Context
import android.util.Log
import com.remmi.app.core.android.system.DailyForecast
import com.remmi.app.core.android.system.HourlyForecast
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.weather.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class WeatherRepository(
    private val client: OpenMeteoClient,
    private val locationManager: WeatherLocationManager
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val prefName = "weather_plugin_prefs"
    private val keySettings = "weather_settings"
    private val keyCachedWeather = "weather_cache"

    fun getSettings(context: Context): WeatherSettings {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(keySettings, null)
        return if (jsonStr != null) {
            try { json.decodeFromString<WeatherSettings>(jsonStr) } catch (e: Exception) { WeatherSettings() }
        } else WeatherSettings()
    }

    fun saveSettings(context: Context, settings: WeatherSettings) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().putString(keySettings, json.encodeToString(settings)).apply()
    }

    fun getCachedWeather(context: Context): WeatherInfo? {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(keyCachedWeather, null)
        return if (jsonStr != null) {
            try {
                val resp = json.decodeFromString<OpenMeteoResponse>(jsonStr)
                mapResponseToWeatherInfo(resp, getSettings(context))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun fetchWeather(context: Context): WeatherInfo? {
        val settings = getSettings(context)
        val location = if (settings.locationMode == LocationMode.DEVICE) {
            locationManager.getCurrentLocation(context)
        } else {
            if (settings.manualLatitude != null && settings.manualLongitude != null) {
                settings.manualLatitude to settings.manualLongitude
            } else null
        }

        if (location == null) {
            Log.w("Remmi", "[WeatherRepository] - Location unavailable")
            return getCachedWeather(context)
        }

        val response = client.getForecast(location.first, location.second, settings.tempUnit, settings.windUnit)
        return if (response != null) {
            // Save to cache
            val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            prefs.edit().putString(keyCachedWeather, json.encodeToString(response)).apply()
            
            val mapped = mapResponseToWeatherInfo(response, settings)
            // Save last update time
            saveSettings(context, settings.copy(lastUpdate = System.currentTimeMillis()))
            mapped
        } else {
            getCachedWeather(context)
        }
    }

    private fun mapResponseToWeatherInfo(resp: OpenMeteoResponse, settings: WeatherSettings): WeatherInfo {
        val current = resp.current!!
        
        val hourly = resp.hourly?.let { h ->
            h.time.indices.take(24).map { i ->
                HourlyForecast(
                    time = h.time[i].substringAfter("T"),
                    temp = h.temperatures[i],
                    icon = WeatherConditionMapper.mapCodeToIcon(h.weatherCodes[i])
                )
            }
        } ?: emptyList()

        val daily = resp.daily?.let { d ->
            d.time.indices.map { i ->
                DailyForecast(
                    day = d.time[i].substring(5), // simplified format
                    minTemp = d.tempMin[i],
                    maxTemp = d.tempMax[i],
                    icon = WeatherConditionMapper.mapCodeToIcon(d.weatherCodes[i])
                )
            }
        } ?: emptyList()

        return WeatherInfo(
            summary = WeatherConditionMapper.mapCodeToSummary(current.weatherCode),
            currentTemp = current.temperature,
            temperatureMin = resp.daily?.tempMin?.firstOrNull() ?: 0.0,
            temperatureMax = resp.daily?.tempMax?.firstOrNull()?.toInt() ?: 0,
            precipitationProbability = resp.daily?.precipitationProbabilityMax?.firstOrNull()?.toDouble()?.div(100.0) ?: 0.0,
            isRainExpected = (resp.daily?.precipitationProbabilityMax?.firstOrNull() ?: 0) > 30,
            icon = WeatherConditionMapper.mapCodeToIcon(current.weatherCode),
            feelsLike = current.feelsLike,
            humidity = current.humidity,
            windSpeed = current.windSpeed,
            uvIndex = 0, // UV not in this specific request but can be added
            visibility = 10.0,
            pressure = 1013,
            sunrise = resp.daily?.sunrise?.firstOrNull()?.substringAfter("T") ?: "",
            sunset = resp.daily?.sunset?.firstOrNull()?.substringAfter("T") ?: "",
            hourlyForecast = hourly,
            dailyForecast = daily
        )
    }

    suspend fun searchLocations(query: String): List<GeocodingResult> {
        return client.searchLocation(query)?.results ?: emptyList()
    }
}
