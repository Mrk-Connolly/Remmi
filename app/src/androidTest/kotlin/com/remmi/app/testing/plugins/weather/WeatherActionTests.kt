package com.remmi.app.testing.plugins.weather

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.weather.WeatherActions
import java.util.UUID

/**
 * WEATHER ACTION TESTS
 */
class FetchWeatherActionTest(
    private val actions: WeatherActions
) : RemmiActionTest {
    override val name: String = "Weather: Fetch Data"
    override val pluginId: String = "weather"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        return try {
            val data = actions.getWeatherData()
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FETCH_WEATHER",
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FETCH_WEATHER",
                status = TestStatus.FAILURE,
                errorMessage = e.message
            )
        }
    }
}
