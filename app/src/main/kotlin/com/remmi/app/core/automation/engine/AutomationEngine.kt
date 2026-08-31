package com.remmi.app.core.automation.engine

import android.util.Log
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.automation.features.LockScreenManager
import com.remmi.app.core.automation.features.databasecleaner.DatabaseCleaner
import com.remmi.app.plugins.dashboard.logic.RemmiWidgetUpdateManager
import com.remmi.app.core.eventBus.*
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.android.services.AndroidServiceManager
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.datetime.Instant

/**
 * AUTOMATION ENGINE
 *
 * Central intelligence of the system that reacts to events (Facts) and issues commands (Intents).
 * Coordinates cross-plugin operations without direct dependencies between plugins.
 */
class AutomationEngine(
    private val eventBus: EventBus,
    private val androidManager: AndroidServiceManager
) : EventListener, CommandListener {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Feature Managers owned by Automation */
    val settingsRepository = AutomationSettingsRepository(androidManager.settingsService)
    val lockScreenManager = LockScreenManager(eventBus, settingsRepository)
    val widgetUpdateManager = RemmiWidgetUpdateManager(androidManager.widgetService)
    val databaseCleaner = DatabaseCleaner(eventBus)

    /** Flag indicating if the engine is currently running and listening to events */
    private var running = false

    /** Temporary state for daily briefing generation */
    private var pendingBriefingTasks: List<TaskItem>? = null
    private var pendingBriefingEvents: List<CalendarItem>? = null
    private var pendingWeather: WeatherInfo? = null


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
        
        // 1. Subscribe Engine
        eventBus.subscribeEvent(this)
        
        // 2. Start sub-features
        lockScreenManager.start()
        
        running = true
    }

    /**                                 Stop
     * Unsubscribe from the Fact channel.
     * */
    fun stop() {
        if (!running) return
        Log.d("Remmi", "[AutomationEngine] - Stopping automation services")
        
        // 1. Stop sub-features
        lockScreenManager.stop()
        
        // 2. Unsubscribe Engine
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
            is WeatherFetchedEvent -> handleWeatherFetched(event)
            is DataFetchedEvent<*> -> handleDataFetched(event)
        }
    }

    private suspend fun handleDataFetched(event: DataFetchedEvent<*>) {
        if (event.source == "database" && event.items.isNotEmpty()) {
            if (event.items[0] is TaskItem && event.correlationId?.contains("cleanup") == true) {
                databaseCleaner.cleanTaskDatabase(event.items.filterIsInstance<TaskItem>())
            }
        }
    }

    /**                                 On Command
     * The engine can also listen for system commands to trigger logic.
     * */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.i("Remmi", "[AutomationEngine] - RECEIVED COMMAND: [${command::class.simpleName}]")
        when (command) {
            is RunDailyBriefingCommand -> startDailyBriefing()
            is RunDatabaseCleanupCommand -> startDatabaseCleanup()
        }
    }

    private suspend fun startDatabaseCleanup() {
        Log.i("Remmi", "[AutomationEngine] - Starting Database Cleanup cycle")
        eventBus.publishCommand(
            FetchAllDataCommand(
                tableName = "tasks",
                serializer = TaskItem.serializer(),
                correlationId = "automation_engine_cleanup",
                source = "automation_engine"
            )
        )
    }

    private suspend fun startDailyBriefing() {
        Log.i("Remmi", "[AutomationEngine] - Starting Daily Briefing Automation")
        
        // Reset state
        pendingBriefingTasks = null
        pendingBriefingEvents = null
        pendingWeather = null
        
        // Request data from plugins and services
        eventBus.publishCommand(FetchTodayTasksCommand())
        eventBus.publishCommand(FetchTodayEventsCommand())
        eventBus.publishCommand(FetchWeatherCommand())
    }

    private suspend fun handleTasksFetched(event: TodayTasksFetchedEvent) {
        pendingBriefingTasks = event.tasks
        checkBriefingReadiness()
        
        // Trigger database cleanup with ALL tasks
        eventBus.publishCommand(
            FetchAllDataCommand(
                tableName = "tasks",
                serializer = TaskItem.serializer(),
                correlationId = "automation_engine_cleanup",
                source = "automation_engine"
            )
        )
    }

    private suspend fun handleEventsFetched(event: TodayEventsFetchedEvent) {
        pendingBriefingEvents = event.events
        checkBriefingReadiness()
    }

    private suspend fun handleWeatherFetched(event: WeatherFetchedEvent) {
        pendingWeather = event.weather
        checkBriefingReadiness()
    }

    private suspend fun checkBriefingReadiness() {
        val tasks = pendingBriefingTasks
        val events = pendingBriefingEvents
        val weather = pendingWeather
        
        if (tasks != null && events != null && weather != null) {
            generateDailyBriefing(tasks, events, weather)
            // Clear state
            pendingBriefingTasks = null
            pendingBriefingEvents = null
            pendingWeather = null
        }
    }

    private suspend fun generateDailyBriefing(
        tasks: List<TaskItem>,
        events: List<CalendarItem>,
        weather: WeatherInfo
    ) {
        Log.i("Remmi", "[AutomationEngine] - Generating final briefing summary")
        
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

        eventBus.publishCommand(
            PostNotificationCommand(
                title = "Your Daily Briefing",
                content = summary
            )
        )

        eventBus.publishEvent(DailyBriefingGeneratedEvent(summary))
    }

    private suspend fun handleTaskCreated(event: TaskCreatedEvent) {
        if (event.priority) {
            val alarmTime = Instant.fromEpochMilliseconds(System.currentTimeMillis() + 3600000)
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

    private suspend fun handleCalendarDeleted(event: CalendarEventDeletedEvent) {
        val linkedAlarmId: String? = null 
        linkedAlarmId?.let { alarmId ->
            eventBus.publishCommand(DeleteAlarmCommand(alarmId = alarmId))
        }
    }
}
