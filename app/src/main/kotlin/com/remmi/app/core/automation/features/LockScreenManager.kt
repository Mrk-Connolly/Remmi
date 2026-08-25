package com.remmi.app.core.automation.features

import android.util.Log
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.events.commands.*
import com.remmi.app.core.events.events.*
import com.remmi.app.core.model.calendar.CalendarItem
import com.remmi.app.core.model.tasks.TaskItem
import kotlinx.datetime.*

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
        
        val summary = buildString {
            append("📅 Weekly Summary\n\n")
            
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            // Events for the week
            append("Schedule:\n")
            if (events.isEmpty()) {
                append("- No events this week\n")
            } else {
                events.sortedBy { it.startingTime }.forEach { event ->
                    val dayStr = if (event.startingDate == today) "Today" else "${event.startingDate.day}/${event.startingDate.monthNumber}"
                    val timeStr = event.startingTime?.let { " at ${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: ""
                    append("- $dayStr$timeStr: ${event.title}\n")
                }
            }
            
            append("\nTasks:\n")
            if (tasks.isEmpty()) {
                append("- No pending tasks\n")
            } else {
                tasks.forEach { task ->
                    val dueStr = task.dueDate?.let {
                        val d = it.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        if (d == today) " (Today)" else " (${d.day}/${d.monthNumber})"
                    } ?: ""
                    append("- ${task.title}$dueStr\n")
                }
            }
        }

        eventBus.publishCommand(
            PostNotificationCommand(
                title = "Remmi Weekly View",
                content = summary,
                useSound = false,
                useVibration = false,
                tag = "lock_screen_summary",
                ongoing = true
            )
        )
    }

    private suspend fun cancelSummary() {
        // Implementation for canceling summary notification
    }
}
