package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.events.events.CalendarEventCreatedEvent
import com.remmi.app.core.events.events.CalendarEventDeletedEvent
import com.remmi.app.core.events.events.CalendarEventUpdatedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Calendar plugin.
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
     * Create and insert a new calendar event
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
        participants: List<String> = emptyList(),
        repeat: List<String> = emptyList(),
        location: List<String> = emptyList(),
        linkedTasks: List<String> = emptyList(),
        linkedAlarm: String? = null
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
                endingDate = endingDate,
                endingTime = endingTime,
                isPriority = isPriority,
                group = group,
                participants = participants,
                repeat = repeat,
                location = location,
                linkedTasks = linkedTasks,
                linkedAlarm = linkedAlarm
            )
            repository.insert(item)
            Log.d(TAG, "Event inserted successfully")

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully created event: ${item.id}. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventCreatedEvent(
                    itemId = item.id,
                    isPriority = item.isPriority
                )
            )

            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert event", e)
            null
        }
    }

    /**                                 Remove Event
     * Delete an event by ID
     * */
    suspend fun removeEvent(id: String): Boolean {
        Log.d("Remmi", "[CalendarActions] - [removeEvent] executed")
        return try {
            repository.delete(id)

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully deleted event: $id. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventDeletedEvent(itemId = id)
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete event", e)
            false
        }
    }

    /**                                 Update Event
     * Update event details
     * */
    suspend fun updateEvent(event: CalendarItem): Boolean {
        Log.d("Remmi", "[CalendarActions] - [updateEvent] executed")
        return try {
            event.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(event)

            // Publish Fact
            Log.i("Remmi", "[CalendarActions] - Successfully updated event: ${event.id}. Publishing event...")
            eventBus?.publishEvent(
                CalendarEventUpdatedEvent(itemId = event.id)
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update event", e)
            false
        }
    }

    /**                                 Get All
     * Retrieve all calendar events
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
     * Retrieve a specific event by ID
     * */
    suspend fun getEvent(id: String): CalendarItem? {
        Log.d("Remmi", "[CalendarActions] - [getEvent] executed")
        return repository.get(id)
    }

    /**                                 Sync
     * Synchronize events with the cloud
     * */
    suspend fun sync(): Boolean {
        Log.d("Remmi", "[CalendarActions] - [sync] executed")
        return try {
            repository.sync()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synchronize calendar", e)
            false
        }
    }

    /**                                 Get Events On
     * Retrieve all events for a specific date
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
     * Retrieve all upcoming events sorted by date
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
     * Retrieve all events scheduled for today
     * */
    suspend fun getTodayEvents(): List<CalendarItem> {
        Log.d("Remmi", "[CalendarActions] - [getTodayEvents] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getEventsOn(today)
    }

    /**                                 Get All Groups
     * Retrieve all unique group names used in events
     * */
    suspend fun getAllGroups(): List<String> {
        Log.d("Remmi", "[CalendarActions] - [getAllGroups] executed")
        return repository.getAll().mapNotNull { it.group }.distinct().sorted()
    }
}
