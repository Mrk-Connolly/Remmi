package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.CreateAlarmCommand
import com.remmi.app.core.events.commands.CreateCalendarEventCommand
import com.remmi.app.core.events.commands.CreateTaskCommand
import com.remmi.app.core.events.commands.DeleteCalendarEventCommand
import com.remmi.app.core.events.commands.DeleteDataCommand
import com.remmi.app.core.events.commands.FetchTodayEventsCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.commands.UpdateCalendarEventCommand
import com.remmi.app.core.events.commands.UpsertDataCommand
import com.remmi.app.core.events.events.CalendarEventCreatedEvent
import com.remmi.app.core.events.events.CalendarEventDeletedEvent
import com.remmi.app.core.events.events.CalendarEventUpdatedEvent
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.events.events.TodayEventsFetchedEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.calendar.ui.screens.CalendarScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * The main entry point for the Calendar plugin.
 */
class CalendarPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: CalendarRepository? = null
    private var _actions: CalendarActions? = null
    private var _authRepository: AuthRepository? = null

    /** Repository for managing Calendar data */
    override val repository: CalendarRepository
        get() = _repository ?: throw IllegalStateException("CalendarPlugin not initialized")

    /** Action controller for calendar logic. */
    override val actions: CalendarActions
        get() = _actions ?: throw IllegalStateException("CalendarPlugin not initialized")

    /** Dashboard widget for calendar. */
    override val widget: RemmiWidget by lazy { CalendarWidget(metadata, actions) }

    /** UI screen for calendar management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[CalendarPlugin] - [Content] executed")
            CalendarScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[CalendarPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[CalendarPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = CalendarRepository(context.databaseManager.service, context.authRepository)
        _repository = repo
        _authRepository = context.authRepository
        
        // Initialize Actions
        _actions = CalendarActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Calendar plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[CalendarPlugin] - Received command: ${command::class.simpleName}")
        when (command) {
            is CreateCalendarEventCommand -> {
                val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                val eventId = java.util.UUID.randomUUID().toString()
                val item = CalendarItem(
                    id = eventId,
                    created = now,
                    modified = now,
                    title = command.title,
                    description = command.description,
                    startingDate = command.startingDate,
                    startingTime = command.startingTime,
                    endingDate = command.endingDate,
                    endingTime = command.endingTime,
                    isPriority = command.isPriority,
                    group = command.group,
                    participants = command.participants,
                    repeat = command.repeat,
                    location = command.location,
                    linkedTasks = command.linkedTasks,
                    linkedAlarm = command.linkedAlarm,
                    userId = _authRepository?.getCurrentUser()?.id
                )
                
                // 1. Request Persistence
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "calendar_TEST",
                        item = item,
                        serializer = CalendarItem.serializer(),
                        source = "calendar"
                    )
                )

                // 2. Publish Fact
                actions.eventBus?.publishEvent(
                    CalendarEventCreatedEvent(
                        itemId = item.id,
                        isPriority = item.isPriority
                    )
                )

                // 3. Handle Linked items
                if (command.createLinkedTask) {
                    actions.eventBus?.publishCommand(
                        CreateTaskCommand(
                            title = "Task for: ${item.title}",
                            description = item.description,
                            dueDate = null, // Or derive from event
                            isPriority = item.isPriority,
                            group = item.group,
                            source = "calendar"
                        )
                    )
                }
                
                if (command.createLinkedAlarm && item.startingTime != null) {
                    // Logic for alarm time calculation
                    val alarmDateTime = kotlinx.datetime.LocalDateTime(item.startingDate, item.startingTime)
                    val alarmTime = alarmDateTime.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
                    actions.eventBus?.publishCommand(
                        CreateAlarmCommand(
                            title = "Alarm: ${item.title}",
                            description = item.description,
                            time = alarmTime,
                            isPriority = item.isPriority,
                            source = "calendar"
                        )
                    )
                }
            }
            
            is UpdateCalendarEventCommand -> {
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "calendar_TEST",
                        item = command.event,
                        serializer = CalendarItem.serializer(),
                        source = "calendar"
                    )
                )
                actions.eventBus?.publishEvent(
                    CalendarEventUpdatedEvent(itemId = command.event.id)
                )
            }
            
            is DeleteCalendarEventCommand -> {
                actions.eventBus?.publishCommand(
                    DeleteDataCommand(
                        tableName = "calendar_TEST",
                        itemId = command.eventId,
                        source = "calendar"
                    )
                )
                // Critical: Notify others for cascading delete
                actions.eventBus?.publishEvent(
                    CalendarEventDeletedEvent(itemId = command.eventId)
                )
            }
            
            is FetchTodayEventsCommand -> {
                Log.d("Remmi", "[CalendarPlugin] - Fetching today's events for automation")
                val events = actions.getTodayEvents()
                actions.eventBus?.publishEvent(TodayEventsFetchedEvent(events))
            }
        }
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        // Calendar might listen for other things in future
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[CalendarPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Calendar Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
        Log.d("Remmi", "Calendar Plugin Loaded")
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[CalendarPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[CalendarPlugin] - [reformat] executed")
        _repository?.clear()
    }
}
