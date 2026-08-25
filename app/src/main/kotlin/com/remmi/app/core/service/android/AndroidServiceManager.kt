package com.remmi.app.core.service.android

import android.content.Context
import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.events.commands.CommandListener
import com.remmi.app.core.events.commands.FetchWeatherCommand
import com.remmi.app.core.events.commands.PostNotificationCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.WeatherFetchedEvent
import com.remmi.app.core.service.android.implementations.SystemAlarmService
import com.remmi.app.core.service.android.implementations.SystemNotificationService
import com.remmi.app.core.service.android.implementations.AndroidWeatherService

/**
 * ANDROID SERVICE MANAGER
 *
 * specialized manager for system-level Android services.
 */
class AndroidServiceManager(
    private val context: Context,
    private val eventBus: EventBus
) : CommandListener {

    /** Specialized Android Services */
    val alarmService: AlarmService = SystemAlarmService(context)
    val notificationService: NotificationService = SystemNotificationService(context)
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

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is FetchWeatherCommand -> {
                Log.i("Remmi", "[AndroidServiceManager] - Fetching weather requested")
                val weather = weatherService.getTodayWeather()
                eventBus.publishEvent(WeatherFetchedEvent(weather))
            }
            is PostNotificationCommand -> {
                Log.i("Remmi", "[AndroidServiceManager] - Posting notification: ${command.title}")
                notificationService.postNotification(
                    title = command.title,
                    content = command.content,
                    useSound = command.useSound,
                    useVibration = command.useVibration,
                    tag = command.tag,
                    ongoing = command.ongoing
                )
            }
        }
    }
}
