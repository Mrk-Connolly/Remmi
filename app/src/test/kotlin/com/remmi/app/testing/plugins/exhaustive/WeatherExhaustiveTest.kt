package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.weather.WeatherPlugin
import com.remmi.app.plugins.weather.WeatherContext
import com.remmi.app.plugins.weather.models.*
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * WEATHER EXHAUSTIVE TEST
 */
open class WeatherExhaustiveTest : BasePluginActionTest() {

    @Test
    fun updateSettingsAndWeatherData_validInput_updatesPluginState() = runTest {
        // Arrange
        WeatherContext.context = context
        val plugin = controller.pluginManager.plugins["weather"] as WeatherPlugin
        val settings = WeatherSettings(
            locationMode = LocationMode.MANUAL,
            manualCityName = "London"
        )

        // Act 1: Update Settings
        plugin.actions.updateSettings(settings)

        // Assert 1
        assertThat(plugin.actions.settings.value.manualCityName).isEqualTo("London")
        
        // Act 2: Update Weather Data
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
        
        // Assert 2
        assertThat(plugin.actions.weatherData.value?.currentTemp).isWithin(5.0).of(20.0)
    }
}
