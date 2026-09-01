package com.remmi.app.core.android.services

import android.content.Context
import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.android.alarms.AlarmService
import com.remmi.app.core.android.alarms.implementations.SystemAlarmService
import com.remmi.app.core.android.notifications.NotificationService
import com.remmi.app.core.android.notifications.implementations.SystemNotificationService
import com.remmi.app.core.android.system.WeatherService
import com.remmi.app.core.android.system.LocationService
import com.remmi.app.core.android.system.OCRService
import com.remmi.app.core.android.system.implementations.AndroidWeatherService
import com.remmi.app.core.android.system.implementations.AndroidLocationService
import com.remmi.app.core.android.system.implementations.AndroidOCRService
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.android.files.AndroidFileService

/**
 * ANDROID SERVICE MANAGER
 *
 * Specialized manager for system-level Android services lifecycle and configuration.
 * Managers only create and configure their dedicated services.
 */
class AndroidServiceManager(
    private val context: Context,
    private val eventBus: EventBus
) {

    /** Specialized Android Services */
    val alarmService: AlarmService = SystemAlarmService(context)
    val notificationService: NotificationService = SystemNotificationService(context)
    val weatherService: WeatherService = AndroidWeatherService(eventBus)
    val locationService: LocationService = AndroidLocationService(eventBus)
    val ocrService: OCRService = AndroidOCRService(context, eventBus)
    val settingsService: SystemSettingsService = SystemSettingsService(context)
    val widgetService: AndroidWidgetService = AndroidWidgetService(context)
    val fileService: FileService = AndroidFileService(context)

    init {
        Log.d("Remmi", "[AndroidServiceManager] - Constructor initialized")
    }

    /**                                 Start
     * Start all system services and subscribe them to the EventBus.
     * */
    fun start() {
        Log.d("Remmi", "[AndroidServiceManager] - Starting services")
        eventBus.subscribeCommand(weatherService)
        eventBus.subscribeCommand(locationService)
        eventBus.subscribeCommand(notificationService)
        eventBus.subscribeCommand(ocrService)
        eventBus.subscribeCommand(alarmService)
    }

    /**                                 Stop
     * Stop all system services and unsubscribe them from the EventBus.
     * */
    fun stop() {
        Log.d("Remmi", "[AndroidServiceManager] - Stopping services")
        eventBus.unsubscribeCommand(weatherService)
        eventBus.unsubscribeCommand(locationService)
        eventBus.unsubscribeCommand(notificationService)
        eventBus.unsubscribeCommand(ocrService)
        eventBus.unsubscribeCommand(alarmService)
    }
}
