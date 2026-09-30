package com.remmi.app.core.android.services

import android.content.Context
import android.util.Log
import com.remmi.app.core.controller.RemmiComponent
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
import com.remmi.app.core.android.google.GoogleAuthService
import com.remmi.app.core.android.media.AudioPlayerService
import com.remmi.app.core.android.media.implementations.AndroidAudioPlayerService
import com.remmi.app.core.android.system.SystemActionService
import com.remmi.app.core.android.system.implementations.AndroidSystemActionService
import android.content.Intent
import android.os.Build

/**
 * ANDROID SERVICE MANAGER
 *
 * Specialized manager for system-level Android services lifecycle and configuration.
 * Managers only create and configure their dedicated services.
 */
class AndroidServiceManager(
    private val context: Context,
    private val eventBus: EventBus,
    val fileService: FileService = AndroidFileService(context)
) : RemmiComponent {

    /** Specialized Android Services */
    val alarmService: AlarmService = SystemAlarmService(context)
    val notificationService: NotificationService = SystemNotificationService(context)
    val weatherService: WeatherService = AndroidWeatherService(eventBus)
    val locationService: LocationService = AndroidLocationService(eventBus)
    val ocrService: OCRService = AndroidOCRService(context, eventBus)
    val settingsService: SystemSettingsService = SystemSettingsService(context)
    val widgetService: AndroidWidgetService = AndroidWidgetService(context)
    val googleAuthService: GoogleAuthService = GoogleAuthService(context, settingsService)

    /** Media and System Action Services */
    val audioPlayerService: AudioPlayerService = AndroidAudioPlayerService()
    val systemActionService: SystemActionService = AndroidSystemActionService(context)

    init {
        Log.d("Remmi", "[AndroidServiceManager] - Constructor initialized")
        // Initialize CallRecordingService static EventBus
        CallRecordingService.eventBus = eventBus
        TranscriptionService.eventBus = eventBus
    }

    /**                                 Start
     * Start all system services and subscribe them to the EventBus with failure isolation.
     * */
    override suspend fun start() {
        Log.d("Remmi", "[AndroidServiceManager] - Starting services")
        runCatching { eventBus.subscribeCommand(weatherService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing weatherService", it) }
        runCatching { eventBus.subscribeCommand(locationService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing locationService", it) }
        runCatching { eventBus.subscribeCommand(notificationService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing notificationService", it) }
        runCatching { eventBus.subscribeCommand(ocrService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing ocrService", it) }
        runCatching { eventBus.subscribeCommand(alarmService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing alarmService", it) }
        runCatching { eventBus.subscribeCommand(audioPlayerService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing audioPlayerService", it) }
        runCatching { eventBus.subscribeCommand(systemActionService) }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed subscribing systemActionService", it) }

        // Start Background Services with failure isolation
        runCatching { startCallRecordingService() }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed starting CallRecordingService", it) }
        runCatching { startTranscriptionService() }.onFailure { Log.e("Remmi", "[AndroidServiceManager] - Failed starting TranscriptionService", it) }
    }

    private fun startCallRecordingService() {
        val intent = Intent(context, CallRecordingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun startTranscriptionService() {
        val intent = Intent(context, TranscriptionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**                                 Stop
     * Stop all system services and unsubscribe them from the EventBus.
     * */
    override fun stop() {
        Log.d("Remmi", "[AndroidServiceManager] - Stopping services")
        eventBus.unsubscribeCommand(weatherService)
        eventBus.unsubscribeCommand(locationService)
        eventBus.unsubscribeCommand(notificationService)
        eventBus.unsubscribeCommand(ocrService)
        eventBus.unsubscribeCommand(alarmService)
        eventBus.unsubscribeCommand(audioPlayerService)
        eventBus.unsubscribeCommand(systemActionService)
    }
}
