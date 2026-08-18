package com.remmi.app.core.automation

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.service.ServiceManager
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.tasks.TaskItem
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * AUTOMATION ENGINE
 *
 * Central intelligence of the system that reacts to events (Facts) and issues commands (Intents).
 * Coordinates cross-plugin operations without direct dependencies between plugins.
 */
class AutomationEngine(
    private val eventBus: EventBus,
    private val serviceManager: ServiceManager
) : EventListener, CommandListener {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Flag indicating if the engine is currently running and listening to events */
    private var running = false

    /** Temporary state for daily briefing generation */
    private var pendingBriefingTasks: List<TaskItem>? = null
    private var pendingBriefingEvents: List<CalendarItem>? = null


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[AutomationEngine] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Subscribe to Fact events on the EventBus.
     * */
    fun start() {
        if (running) return
        Log.d("Remmi", "[AutomationEngine] - Starting automation services")
        eventBus.subscribeEvent(this)
        running = true
    }

    /**                                 Stop
     * Unsubscribe from the Fact channel.
     * */
    fun stop() {
        if (!running) return
        Log.d("Remmi", "[AutomationEngine] - Stopping automation services")
        eventBus.unsubscribeEvent(this)
        running = false
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Event
     * Handle Facts distributed via the EventBus.
     * Decisions made here will result in Commands being published.
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.i("Remmi", "[AutomationEngine] - RECEIVED FACT: [${event.type}] from [${event.source}]")
        
        when (event) {
            is TaskCreatedEvent -> handleTaskCreated(event)
            is CalendarEventDeletedEvent -> handleCalendarDeleted(event)
            is TodayTasksFetchedEvent -> handleTasksFetched(event)
            is TodayEventsFetchedEvent -> handleEventsFetched(event)
        }
    }

    /**                                 On Command
     * The engine can also listen for system commands to trigger logic.
     * */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.i("Remmi", "[AutomationEngine] - RECEIVED COMMAND: [${command::class.simpleName}]")
        when (command) {
            is RunDailyBriefingCommand -> startDailyBriefing()
        }
    }

    private suspend fun startDailyBriefing() {
        Log.i("Remmi", "[AutomationEngine] - Starting Daily Briefing Automation")
        
        // Reset state
        pendingBriefingTasks = null
        pendingBriefingEvents = null
        
        // Request data from plugins
        eventBus.publishCommand(FetchTodayTasksCommand())
        eventBus.publishCommand(FetchTodayEventsCommand())
        
        // We'll wait for the Fetched events in onEvent
    }

    private suspend fun handleTasksFetched(event: TodayTasksFetchedEvent) {
        pendingBriefingTasks = event.tasks
        checkBriefingReadiness()
    }

    private suspend fun handleEventsFetched(event: TodayEventsFetchedEvent) {
        pendingBriefingEvents = event.events
        checkBriefingReadiness()
    }

    private suspend fun checkBriefingReadiness() {
        val tasks = pendingBriefingTasks
        val events = pendingBriefingEvents
        
        if (tasks != null && events != null) {
            generateDailyBriefing(tasks, events)
            // Clear state
            pendingBriefingTasks = null
            pendingBriefingEvents = null
        }
    }

    private suspend fun generateDailyBriefing(tasks: List<TaskItem>, events: List<CalendarItem>) {
        Log.i("Remmi", "[AutomationEngine] - Generating final briefing summary")
        
        val weather = serviceManager.weatherService.getTodayWeather()
        
        val summary = buildString {
            append("Good morning!\n\n")
            
            append("Today's Schedule:\n")
            if (events.isEmpty()) {
                append("- No calendar events\n")
            } else {
                events.sortedBy { it.startingTime }.forEach { event ->
                    val time = event.startingTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: "All day"
                    append("- $time: ${event.title}\n")
                }
            }
            
            append("\nTasks to complete:\n")
            if (tasks.isEmpty()) {
                append("- No tasks pending\n")
            } else {
                tasks.forEach { task ->
                    append("- ${task.title}${if (task.isPriority) " (Priority)" else ""}\n")
                }
            }
            
            append("\nWeather:\n")
            append("${weather.summary}\n")
            append("Temp: ${weather.temperatureMin}°C - ${weather.temperatureMax}°C\n")
            
            if (weather.isRainExpected) {
                append("\nRecommendation: Rain expected. Take an umbrella!")
            } else {
                append("\nRecommendation: No rain expected today.")
            }
        }

        Log.d("Remmi", "[AutomationEngine] - Briefing summary:\n$summary")
        
        // Post Notification
        serviceManager.notificationService.postNotification(
            title = "Your Daily Briefing",
            content = summary
        )

        // Publish Event
        eventBus.publishEvent(DailyBriefingGeneratedEvent(summary))
    }

    /**                                 Handle Task Created
     * Automation Rule: If a high priority task is created, ensure an alarm is set.
     * */
    private suspend fun handleTaskCreated(event: TaskCreatedEvent) {
        if (event.priority) {
            Log.i("Remmi", "[AutomationEngine] - High priority task created! Triggering CreateAlarmCommand...")
            
            // Automation: Create an alarm for high priority tasks
            // In a real app, we might use the task's due date.
            // Here we just set an alarm for a few hours from now as a demonstration.
            val alarmTime = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis() + 3600000) // +1 hour
            
            eventBus.publishCommand(
                CreateAlarmCommand(
                    title = "Priority Task Reminder",
                    description = "Don't forget to work on task: ${event.taskId}",
                    time = alarmTime,
                    isPriority = true,
                    source = "automation"
                )
            )
        }
    }

    /**                                 Handle Calendar Deleted
     * Automation Rule: Cleanup linked resources when a calendar event is removed.
     * */
    private suspend fun handleCalendarDeleted(event: CalendarEventDeletedEvent) {
        Log.i("Remmi", "[AutomationEngine] - Calendar event deleted. Checking for linked Alarms...")
        
        // TODO: In a real implementation, we would lookup linkedAlarmId from a mapping service/db
        val linkedAlarmId: String? = null 
        
        linkedAlarmId?.let { alarmId ->
            Log.i("Remmi", "[AutomationEngine] - Issuing DeleteAlarmCommand for: $alarmId")
            eventBus.publishCommand(
                DeleteAlarmCommand(alarmId = alarmId)
            )
        }
    }
}
