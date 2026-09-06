package com.remmi.app.plugins.weather

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.plugins.weather.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Action controller for the Weather plugin via EventBus.
 */
class WeatherActions(
    private val repository: WeatherRepository,
    override val id: String = "weather_actions",
    override val name: String = "Weather Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null

    /** Settings observed by UI */
    private val _settings = MutableStateFlow(WeatherSettings())
    val settings = _settings.asStateFlow()

    /** Current weather state observed by UI */
    val weatherData = mutableStateOf<WeatherInfo?>(null)
    val isLoading = mutableStateOf(false)
    
    val searchResults = mutableStateOf<List<GeocodingResult>>(emptyList())

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
        
        // Refresh settings from disk as they might have changed
        WeatherContext.context?.let { ctx ->
            _settings.value = repository.getSettings(ctx)
        }
    }

    suspend fun updateSettings(newSettings: WeatherSettings) {
        val context = WeatherContext.context ?: return
        repository.saveSettings(context, newSettings)
        _settings.value = newSettings
        fetchWeatherData()
    }

    suspend fun searchLocations(query: String) {
        if (query.length < 3) return
        searchResults.value = repository.searchLocations(query)
    }

    suspend fun selectLocation(result: GeocodingResult) {
        val current = _settings.value
        updateSettings(current.copy(
            locationMode = LocationMode.MANUAL,
            manualLatitude = result.latitude,
            manualLongitude = result.longitude,
            manualCityName = result.name
        ))
        searchResults.value = emptyList()
    }
}
