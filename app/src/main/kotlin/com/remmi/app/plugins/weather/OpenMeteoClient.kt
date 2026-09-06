package com.remmi.app.plugins.weather

import android.util.Log
import com.remmi.app.plugins.weather.models.GeocodingResponse
import com.remmi.app.plugins.weather.models.OpenMeteoResponse
import com.remmi.app.plugins.weather.models.TemperatureUnit
import com.remmi.app.plugins.weather.models.WindSpeedUnit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class OpenMeteoClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        tempUnit: TemperatureUnit,
        windUnit: WindSpeedUnit
    ): OpenMeteoResponse? {
        return try {
            val tUnit = if (tempUnit == TemperatureUnit.FAHRENHEIT) "fahrenheit" else "celsius"
            val wUnit = if (windUnit == WindSpeedUnit.MPH) "mph" else "kmh"

            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m" +
                    "&hourly=temperature_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum,precipitation_probability_max,wind_speed_10m_max" +
                    "&timezone=auto&temperature_unit=$tUnit&wind_speed_unit=$wUnit"

            Log.d("Remmi", "[OpenMeteoClient] - Requesting forecast: $url")
            client.get(url).body<OpenMeteoResponse>()
        } catch (e: Exception) {
            Log.e("Remmi", "[OpenMeteoClient] - Error fetching forecast: ${e.message}")
            null
        }
    }

    suspend fun searchLocation(query: String): GeocodingResponse? {
        return try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$query&count=5&language=en&format=json"
            Log.d("Remmi", "[OpenMeteoClient] - Searching location: $url")
            client.get(url).body<GeocodingResponse>()
        } catch (e: Exception) {
            Log.e("Remmi", "[OpenMeteoClient] - Error searching location: ${e.message}")
            null
        }
    }
}
