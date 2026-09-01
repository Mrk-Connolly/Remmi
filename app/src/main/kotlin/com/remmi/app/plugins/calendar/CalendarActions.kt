package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.eventBus.*
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.CalendarEventCreatedEvent
import com.remmi.app.core.eventBus.events.CalendarEventDeletedEvent
import com.remmi.app.core.eventBus.events.CalendarEventUpdatedEvent
import com.remmi.app.core.eventBus.events.LinkedCreationRequest
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.models.CalendarItem
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Calendar plugin via EventBus.
 */
class CalendarActions(
    private val repository: CalendarRepository,
    override val id: String = "calendar_actions",
    override val name: String = "Calendar Actions"
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system event bus */
    override var eventBus: EventBus? = null

    private var _cachedGroups = mutableListOf<CalendarGroup>()

    companion object {
        private const val TAG = "CalendarActions"
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Calendar Actions
     * */
    init {
        Log.d("Remmi", "[CalendarActions] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Add Event
     * Create and insert a new calendar event via commands
     * */
    suspend fun addEvent(
        id: String = UUID.randomUUID().toString(),
        title: String,
        description: String,
        startingDate: LocalDate,
        startingTime: LocalTime? = null,
        endingDate: LocalDate? = null,
        endingTime: LocalTime? = null,
        isPriority: Boolean = false,
        group: String? = null,
        isRepeatable: Boolean = false,
        repeatableType: String? = null,
        participants: List<String> = emptyList(),
        repeat: List<String> = emptyList(),
        location: List<String> = emptyList(),
        createAlarm: Boolean = false,
        createTask: Boolean = false,
        createLocation: Boolean = false,
        createContact: Boolean = false,
        correlationId: String? = null,
        causationId: String? = null,
        creationContext: CreationContext? = null
    ): String? {
        Log.d("Remmi", "[CalendarActions] - [addEvent] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val item = CalendarItem(
                id = id,
                created = now,
                modified = now,
                title = title,
                description = description,
                startingDate = startingDate,
                startingTime = startingTime,
                endingDate = endingDate ?: startingDate,
                endingTime = endingTime,
                isPriority = isPriority,
                group = group,
                isRepeatable = isRepeatable,
                repeatableType = repeatableType,
                participants = participants,
                repeat = repeat,
                location = location,
                createAlarm = createAlarm,
                createTask = createTask,
                createLocation = createLocation,
                createContact = createContact
            )
            
            // 1. Update local cache
            repository.add(item)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "calendar",
                    item = item,
                    serializer = CalendarItem.serializer(),
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully created event: ${item.id}. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventCreatedEvent(
                    itemId = item.id,
                    isPriority = item.isPriority,
                    linkedRequests = LinkedCreationRequest(
                        createAlarm = item.createAlarm,
                        createTask = item.createTask,
                        createLocation = item.createLocation,
                        createContact = item.createContact
                    ),
                    correlationId = correlationId,
                    causationId = causationId,
                    creationContext = creationContext
                )
            )

            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert event", e)
            null
        }
    }

    /**                                 Remove Event
     * Delete an event by ID via commands
     * */
    suspend fun removeEvent(
        id: String,
        correlationId: String? = null,
        causationId: String? = null,
        deletionContext: DeletionContext? = null
    ): Boolean {
        Log.d("Remmi", "[CalendarActions] - [removeEvent] executed")
        return try {
            // 1. Remove from local cache
            repository.remove(id)
            
            // 2. Persist deletion to cloud
            eventBus?.publishCommand(
                DeleteDataCommand(
                    tableName = "calendar",
                    itemId = id,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully deleted event: $id. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventDeletedEvent(
                    itemId = id,
                    correlationId = correlationId,
                    causationId = causationId,
                    deletionContext = deletionContext
                )
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete event", e)
            false
        }
    }

    /**                                 Update Event
     * Update event details via commands
     * */
    suspend fun updateEvent(
        event: CalendarItem,
        correlationId: String? = null,
        causationId: String? = null
    ): Boolean {
        Log.d("Remmi", "[CalendarActions] - [updateEvent] executed")
        return try {
            val updatedEvent = event.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
            
            // 1. Update local cache
            repository.update(updatedEvent)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "calendar",
                    item = updatedEvent,
                    serializer = CalendarItem.serializer(),
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully updated event: ${event.id}. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventUpdatedEvent(
                    itemId = event.id,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update event", e)
            false
        }
    }

    /**                                 Get All
     * Retrieve all calendar events from local cache
     * */
    suspend fun getAllEvents(): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getAllEvents] executed")
        return try {
            repository.getAll()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve events", e)
            emptyList()
        }
    }

    /**                                 Get Event
     * Retrieve a specific event by ID from local cache
     * */
    suspend fun getEvent(id: String): CalendarItem? {
        Log.d("Remmi", "[CalendarActions] - [getEvent] executed")
        return repository.get(id)
    }

    /**                                 Sync
     * Synchronize events with the cloud via command
     * */
    suspend fun sync(): Boolean {
        Log.d("Remmi", "[CalendarActions] - [sync] executed")
        return try {
            eventBus?.publishCommand(
                FetchAllDataCommand(
                    tableName = "calendar",
                    serializer = CalendarItem.serializer()
                )
            )
            // Also sync groups
            eventBus?.publishCommand(
                FetchAllDataCommand(
                    tableName = "calendar_groups",
                    serializer = CalendarGroup.serializer()
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synchronize calendar", e)
            false
        }
    }

    /**                                 Get Events On
     * Retrieve all events for a specific date from cache
     * */
    suspend fun getEventsOn(date: LocalDate): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getEventsOn] executed")
        return try {
            repository.getAll().filter { it.startingDate == date }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query events for date", e)
            emptyList()
        }
    }

    /**                                 Get Upcoming
     * Retrieve all upcoming events sorted by date from cache
     * */
    suspend fun getUpcomingEvents(): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getUpcomingEvents] executed")
        return try {
            repository.getAll()
                .sortedBy { it.startingDate }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve upcoming events", e)
            emptyList()
        }
    }

    /**                                 Get Today
     * Retrieve all events scheduled for today from cache
     * */
    suspend fun getTodayEvents(): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getTodayEvents] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getEventsOn(today)
    }

    /**                                 Get Weekly
     * Retrieve all events for the next 7 days from cache
     * */
    suspend fun getWeeklyEvents(): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getWeeklyEvents] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val nextWeek = today.plus(7, DateTimeUnit.DAY)
        return repository.getAll().filter { it.startingDate in today..nextWeek }.sortedBy { it.startingDate }
    }

    /**                                 Get All Groups
     * Retrieve all unique group names used in events
     * */
    suspend fun getAllGroups(): List<String> {
        Log.d("Remmi", "[CalendarActions] - [getAllGroups] executed")
        return _cachedGroups.map { it.name }.distinct().sorted()
    }

    /**                                 Get Calendar Groups
     * Retrieve all calendar groups from internal cache
     * */
    fun getCalendarGroups(): List<CalendarGroup> {
        Log.d("Remmi", "[CalendarActions] - [getCalendarGroups] executed")
        return _cachedGroups.toList()
    }

    /**                                 Update Groups
     * Update the internal groups cache (called by plugin when DataFetchedEvent arrives)
     */
    fun updateGroups(groups: List<CalendarGroup>) {
        _cachedGroups.clear()
        _cachedGroups.addAll(groups)
    }

    /**                                 Add Calendar Group
     * Create and insert a new calendar group via command
     * */
    suspend fun addCalendarGroup(name: String, colorHex: String): String? {
        Log.d("Remmi", "[CalendarActions] - [addCalendarGroup] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val group = CalendarGroup(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                name = name,
                colorHex = colorHex
            )
            
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "calendar_groups",
                    item = group,
                    serializer = CalendarGroup.serializer(),
                    source = "calendar"
                )
            )
            group.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add calendar group", e)
            null
        }
    }
}
