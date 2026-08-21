package com.remmi.app.core.android

import android.content.Context
import android.util.Log
import com.remmi.app.core.android.implementations.AndroidAlarmService
import com.remmi.app.core.android.implementations.AndroidWeatherService

/**
 * ANDROID SERVICE MANAGER
 *
 * specialized manager for system-level Android services.
 */
class AndroidServiceManager(private val context: Context) {

    /** Specialized Android Services */
    val alarmService: AlarmService = AndroidAlarmService(context)
    val notificationService: NotificationService = AndroidAlarmService(context)
    val weatherService: WeatherService = AndroidWeatherService()

    init {
        Log.d("Remmi", "[AndroidServiceManager] - Constructor initialized")
    }

    /**                                 Start
     * Start all system services.
     * */
    fun start() {
        Log.d("Remmi", "[AndroidServiceManager] - Starting services")
    }

    /**                                 Stop
     * Stop all system services.
     * */
    fun stop() {
        Log.d("Remmi", "[AndroidServiceManager] - Stopping services")
    }
}
