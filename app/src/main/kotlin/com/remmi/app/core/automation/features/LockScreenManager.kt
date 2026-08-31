package com.remmi.app.core.automation.features

import android.util.Log
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem

/**
 * LOCK SCREEN MANAGER
 *
 * Coordinates the display of weekly schedule and tasks on the lock screen via a persistent notification.
 */
class LockScreenManager(
    private val eventBus: EventBus,
    private val repository: AutomationSettingsRepository
) : EventListener, CommandListener {

    private var pendingTasks: List<TaskItem>? = null
    private var pendingEvents: List<CalendarItem>? = null

    companion object {
        private const val TAG = "LockScreenManager"
    }

    fun start() {
        Log.d(TAG, "Starting LockScreenManager")
        eventBus.subscribeEvent(this)
        eventBus.subscribeCommand(this)
    }

    fun stop() {
        Log.d(TAG, "Stopping LockScreenManager")
        eventBus.unsubscribeEvent(this)
        eventBus.unsubscribeCommand(this)
    }

    override suspend fun onEvent(event: RemmiEvent) {
        if (!repository.isLockScreenSummaryEnabled()) return

        when (event) {
            is CalendarEventCreatedEvent, is CalendarEventUpdatedEvent, is CalendarEventDeletedEvent,
            is TaskCreatedEvent, is TaskUpdatedEvent, is TaskDeletedEvent -> {
                Log.d(TAG, "Data changed, triggering refresh")
                refreshSummary()
            }
            is WeeklyEventsFetchedEvent -> {
                pendingEvents = event.events
                checkReadiness()
            }
            is WeeklyTasksFetchedEvent -> {
                pendingTasks = event.tasks
                checkReadiness()
            }
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        if (command is UpdateLockScreenSummaryCommand) {
            // Can be used to manually trigger updates if needed
        }
    }

    suspend fun refreshSummary() {
        if (!repository.isLockScreenSummaryEnabled()) {
            cancelSummary()
            return
        }
        
        Log.d(TAG, "Requesting weekly data")
        pendingTasks = null
        pendingEvents = null
        eventBus.publishCommand(FetchWeeklyEventsCommand())
        eventBus.publishCommand(FetchWeeklyTasksCommand())
    }

    private suspend fun checkReadiness() {
        val tasks = pendingTasks
        val events = pendingEvents
        if (tasks != null && events != null) {
            updateNotification(tasks, events)
            pendingTasks = null
            pendingEvents = null
        }
    }

    private suspend fun updateNotification(tasks: List<TaskItem>, events: List<CalendarItem>) {
        if (!repository.isLockScreenSummaryEnabled()) return
        
        Log.i(TAG, "Updating lock screen notification")
        
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }
        val activeTasks = tasks.filter { !it.completed }

        val liveUpdateSummary = if (activeTasks.isEmpty()) {
            "All tasks complete for today!"
        } else {
            activeTasks.joinToString("\n") { "• ${it.title}" }
        }

        eventBus.publishCommand(
            PostLiveUpdateCommand(
                title = "Daily Progress: $completedTasks/$totalTasks",
                content = liveUpdateSummary,
                progress = completedTasks,
                maxProgress = totalTasks.coerceAtLeast(1),
                tag = "lock_screen_summary",
                source = "lock_screen"
            )
        )
    }

    private suspend fun cancelSummary() {
        // Implementation for canceling summary notification
    }
}
