package com.remmi.app.core.automation.features.lockscreenManager

import android.util.Log
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.FetchTodayEventsCommand
import com.remmi.app.core.eventBus.commands.FetchTodayTasksCommand
import com.remmi.app.core.eventBus.commands.FetchWeatherCommand
import com.remmi.app.core.eventBus.commands.PostNotificationCommand
import com.remmi.app.core.eventBus.commands.CancelNotificationCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.UpdateLockScreenSummaryCommand
import com.remmi.app.core.eventBus.events.CalendarEventCreatedEvent
import com.remmi.app.core.eventBus.events.CalendarEventDeletedEvent
import com.remmi.app.core.eventBus.events.CalendarEventUpdatedEvent
import com.remmi.app.core.eventBus.events.EventListener
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.eventBus.events.TaskCreatedEvent
import com.remmi.app.core.eventBus.events.TaskDeletedEvent
import com.remmi.app.core.eventBus.events.TaskUpdatedEvent
import com.remmi.app.core.eventBus.events.TodayEventsFetchedEvent
import com.remmi.app.core.eventBus.events.TodayTasksFetchedEvent
import com.remmi.app.core.eventBus.events.WeatherFetchedEvent
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * LOCK SCREEN MANAGER
 *
 * Coordinates the display of daily schedule, tasks, and weather on the lock screen via a persistent notification.
 */
class LockScreenManager(
    private val eventBus: EventBus,
    private val repository: AutomationSettingsRepository
) : EventListener, CommandListener {

    companion object {
        private const val TAG = "LockScreenManager"
        private const val NOTIFICATION_TAG = "lock_screen_summary"
    }

    fun start() {
        Log.d(TAG, "Starting LockScreenManager")
        eventBus.subscribeEvent(this)
        eventBus.subscribeCommand(this)
        
        // Initial refresh
        CoroutineScope(Dispatchers.IO).launch {
            refreshSummary()
        }
    }

    fun stop() {
        Log.d(TAG, "Stopping LockScreenManager")
        eventBus.unsubscribeEvent(this)
        eventBus.unsubscribeCommand(this)
    }

    override suspend fun onEvent(event: RemmiEvent) {
        if (!repository.isLockScreenSummaryEnabled()) return

        when (event) {
            is com.remmi.app.core.eventBus.events.DailyBriefingGeneratedEvent -> {
                Log.d(TAG, "Received new summary from AutomationEngine")
                updateNotification(event.summary)
            }
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        if (command is UpdateLockScreenSummaryCommand) {
            // AutomationEngine now handles the gathering and building. 
            // We just wait for the Fact.
        }
    }

    suspend fun refreshSummary() {
        if (!repository.isLockScreenSummaryEnabled()) {
            cancelSummary()
            return
        }
        
        Log.d(TAG, "Requesting summary update from AutomationEngine")
        eventBus.publishCommand(com.remmi.app.core.eventBus.commands.RunDailyBriefingCommand())
    }

    private suspend fun updateNotification(summary: String) {
        if (!repository.isLockScreenSummaryEnabled()) return
        
        Log.i(TAG, "Updating persistent lock screen notification with finalized summary")
        
        eventBus.publishCommand(
            PostNotificationCommand(
                title = "Daily Summary",
                content = summary.trim(),
                useSound = false,
                useVibration = false,
                tag = NOTIFICATION_TAG,
                ongoing = true,
                source = "lock_screen"
            )
        )
    }

    private suspend fun cancelSummary() {
        Log.d(TAG, "Canceling lock screen summary notification")
        eventBus.publishCommand(CancelNotificationCommand(tag = NOTIFICATION_TAG))
    }
}
