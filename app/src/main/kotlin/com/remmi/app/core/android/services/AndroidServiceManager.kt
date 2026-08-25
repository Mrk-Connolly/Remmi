package com.remmi.app.core.android.services

import android.content.Context
import android.util.Log
import com.remmi.app.core.eventBus.*
import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.core.eventBus.commands.PostNotificationCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.WeatherFetchedEvent
import com.remmi.app.core.android.alarms.AlarmService
import com.remmi.app.core.android.alarms.implementations.SystemAlarmService
import com.remmi.app.core.android.notifications.NotificationService
import com.remmi.app.core.android.notifications.implementations.SystemNotificationService
import com.remmi.app.core.android.system.WeatherService
import com.remmi.app.core.android.system.implementations.AndroidWeatherService
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.android.files.AndroidFileService

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
    val settingsService: SystemSettingsService = SystemSettingsService(context)
    val widgetService: AndroidWidgetService = AndroidWidgetService(context)
    val fileService: FileService = AndroidFileService(context)

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
