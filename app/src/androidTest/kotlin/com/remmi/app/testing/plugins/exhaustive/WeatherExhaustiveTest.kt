package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.weather.WeatherPlugin
import com.remmi.app.plugins.weather.models.*
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * WEATHER EXHAUSTIVE TEST
 */
class WeatherExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testWeatherState_Success() = runTest {
        val plugin = controller.pluginManager.plugins["weather"] as WeatherPlugin
        
        // 1. Update Settings
        val settings = WeatherSettings(
            locationMode = LocationMode.MANUAL,
            manualCityName = "London"
        )
        plugin.actions.updateSettings(settings)
        assertThat(plugin.actions.settings.value.manualCityName).isEqualTo("London")
        
        // 2. Update Weather Data
        val info = WeatherInfo(
            summary = "Sunny",
            currentTemp = 20.0,
            temperatureMin = 15.0,
            temperatureMax = 25,
            precipitationProbability = 0.1,
            isRainExpected = false,
            icon = "sunny",
            feelsLike = 21.0,
            humidity = 50,
            windSpeed = 5.0,
            uvIndex = 5,
            visibility = 10.0,
            pressure = 1013,
            sunrise = "06:00",
            sunset = "20:00",
            hourlyForecast = emptyList(),
            dailyForecast = emptyList()
        )
        plugin.actions.updateWeatherData(info)
        assertThat(plugin.actions.weatherData.value?.currentTemp).isEqualTo(20.0)
    }
}
