package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.calendar.ui.screens.CalendarScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main entry point for the Calendar plugin via EventBus.
 */
class CalendarPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: CalendarRepository = CalendarRepository()
    private val _actions: CalendarActions = CalendarActions(_repository).apply {
        this.eventBus = this@CalendarPlugin.eventBus
    }

    /** Repository for managing Calendar data */
    override val repository: CalendarRepository get() = _repository

    /** Action controller for calendar logic. */
    override val actions: CalendarActions get() = _actions

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
    override suspend fun initialize() {
        Log.d("Remmi", "[CalendarPlugin] - Initializing")
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
                    isRepeatable = command.isRepeatable,
                    repeatableType = command.repeatableType,
                    participants = command.participants,
                    repeat = command.repeat,
                    location = command.location,
                    createAlarm = command.createLinkedAlarm,
                    createTask = command.createLinkedTask,
                    createLocation = command.createLinkedLocation,
                    createContact = command.createLinkedContact,
                    userId = null
                )
                
                // 1. Request Persistence
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "calendar",
                        item = item,
                        serializer = CalendarItem.serializer(),
                        source = "calendar",
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId,
                        creationContext = command.creationContext ?: CreationContext.PRIMARY
                    )
                )

                // 2. Publish Fact (Listeners will react to create linked items)
                actions.eventBus?.publishEvent(
                    CalendarEventCreatedEvent(
                        itemId = item.id,
                        isPriority = item.isPriority,
                        linkedRequests = LinkedCreationRequest(
                            createAlarm = item.createAlarm,
                            createTask = item.createTask,
                            createLocation = item.createLocation,
                            createContact = item.createContact
                        ),
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId,
                        creationContext = command.creationContext ?: CreationContext.PRIMARY
                    )
                )
            }
            
            is UpdateCalendarEventCommand -> {
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "calendar",
                        item = command.event,
                        serializer = CalendarItem.serializer(),
                        source = "calendar",
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId
                    )
                )
                actions.eventBus?.publishEvent(
                    CalendarEventUpdatedEvent(
                        itemId = command.event.id,
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId
                    )
                )
            }
            
            is DeleteCalendarEventCommand -> {
                actions.eventBus?.publishCommand(
                    DeleteDataCommand(
                        tableName = "calendar",
                        itemId = command.eventId,
                        source = "calendar",
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId,
                        deletionContext = command.deletionContext ?: com.remmi.app.core.eventBus.DeletionContext.PRIMARY
                    )
                )
                // Critical: Notify others for cascading delete
                actions.eventBus?.publishEvent(
                    CalendarEventDeletedEvent(
                        itemId = command.eventId,
                        correlationId = command.correlationId ?: command.commandId,
                        causationId = command.commandId,
                        deletionContext = command.deletionContext ?: com.remmi.app.core.eventBus.DeletionContext.PRIMARY
                    )
                )
            }
            
            is FetchTodayEventsCommand -> {
                Log.d("Remmi", "[CalendarPlugin] - Fetching today's events for automation")
                val events = actions.getTodayEvents()
                actions.eventBus?.publishEvent(TodayEventsFetchedEvent(events))
            }
            
            is FetchWeeklyEventsCommand -> {
                Log.d("Remmi", "[CalendarPlugin] - Fetching weekly events for lock screen")
                val events = actions.getWeeklyEvents()
                actions.eventBus?.publishEvent(WeeklyEventsFetchedEvent(events))
            }
        }
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[CalendarPlugin] - Received event: ${event::class.simpleName}")
        when (event) {
            is TaskDeletedEvent -> {
                Log.i("Remmi", "[CalendarPlugin] - Source task ${event.taskId} deleted. Cleaning up linked events...")
                eventBus.publishCommand(
                    FetchDataBySourceCommand(
                        tableName = "calendar",
                        sourcePlugin = "tasks",
                        sourceItemId = event.taskId,
                        serializer = CalendarItem.serializer(),
                        correlationId = "calendar_plugin_cleanup_${event.taskId}",
                        causationId = event.eventId,
                        source = "calendar_plugin"
                    )
                )
            }
            is DataFetchedEvent<*> -> {
                handleDataFetched(event)
            }
        }
    }

    private fun handleDataFetched(event: DataFetchedEvent<*>) {
        if (event.items.isNotEmpty()) {
            val first = event.items[0]
            if (first is CalendarItem) {
                // If it's a cleanup response
                if (event.source == "calendar_plugin" && event.correlationId?.startsWith("calendar_plugin_cleanup") == true) {
                    event.items.forEach { item ->
                        if (item is CalendarItem) {
                            CoroutineScope(Dispatchers.IO).launch {
                                actions.eventBus?.publishCommand(
                                    DeleteCalendarEventCommand(
                                        eventId = item.id,
                                        source = "calendar_cleanup",
                                        correlationId = event.correlationId,
                                        causationId = event.eventId,
                                        deletionContext = com.remmi.app.core.eventBus.DeletionContext.LINKED_CLEANUP
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // Global sync or fetch
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<CalendarItem>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[CalendarPlugin] - Updated repository with ${event.items.size} events")
                }
            } else if (first is CalendarGroup) {
                @Suppress("UNCHECKED_CAST")
                _actions.updateGroups(event.items as List<CalendarGroup>)
                Log.d("Remmi", "[CalendarPlugin] - Updated groups with ${event.items.size} items")
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[CalendarPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Calendar Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Calendar Plugin Loaded")
    }

    /**                                   Refresh
     * Sync calendar events with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[CalendarPlugin] - Refreshing data")
        actions.sync()
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
        _repository.clear()
        eventBus.publishCommand(
            DeleteDataCommand(
                tableName = "calendar",
                itemId = "all",
                source = "calendar"
            )
        )
        eventBus.publishCommand(
            DeleteDataCommand(
                tableName = "calendar_groups",
                itemId = "all",
                source = "calendar"
            )
        )
    }
}
