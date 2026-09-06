package com.remmi.app.plugins.weather

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import kotlinx.coroutines.*

class WeatherAutomation(
    private val eventBus: EventBus
) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        if (job != null) return
        Log.d("Remmi", "[WeatherAutomation] - Starting hourly weather refresh")
        job = scope.launch {
            while (isActive) {
                Log.i("Remmi", "[WeatherAutomation] - Triggering weather refresh command")
                eventBus.publishCommand(FetchWeatherCommand())
                delay(3600000) // 1 hour
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
