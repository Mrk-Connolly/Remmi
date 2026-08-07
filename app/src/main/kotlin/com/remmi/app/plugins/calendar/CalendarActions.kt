package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.models.Person
import com.remmi.app.plugins.tasks.TaskItem
import com.remmi.app.plugins.tasks.TasksRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlinx.datetime.Instant

/**
 * Action controller for the Calendar plugin.
 *
 * This class handles all business logic related to calendar events, including
 * CRUD operations, synchronization with the cloud, and date-based queries.
 */
class CalendarActions(
    private val repository: CalendarRepository,
    private val tasksRepository: TasksRepository
) : RemmiAction {

    companion object {
        private const val TAG = "CalendarActions"
    }

    /* --------------------------
     * CRUD Operations
     * -------------------------- */

    /**
     * Adds a new event to the calendar.
     */
    suspend fun addEvent(
        id: String = UUID.randomUUID().toString(),
        title: String,
        description: String,
        startingTime: Instant? = null,
        endingTime: Instant? = null,
        priority: Priority = Priority.NORMAL,
        repeat: RepeatRule? = null,
        location: Location? = null,
        participants: List<Person> = emptyList(),
        linkedTasks: List<String> = emptyList(),
        linkedAlarm: String? = null
    ): String? {
        return try {
            val item = CalendarItem(
                id = id,
                created = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                title = title,
                description = description,
                startingTime = startingTime,
                endingTime = endingTime,
                priority = priority,
                repeat = repeat,
                location = location,
                participants = participants.toMutableList(),
                linkedTasks = linkedTasks.toMutableList(),
                linkedAlarm = linkedAlarm
            )
            repository.insert(item)
            Log.d(TAG, "Event inserted successfully")
            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert event", e)
            null
        }
    }

    /**
     * Legacy addEvent method for backward compatibility if needed,
     * or can be removed if all callers are updated.
     */
    suspend fun addEvent(
        title: String,
        description: String,
        day: String,
        month: String,
        year: String,
        startTime: String,
        endTime: String,
        priority: Priority
    ): Boolean {
        // ... (kept for now to avoid breaking existing UI until updated)
        return try {
            val timeZone = TimeZone.currentSystemDefault()
            val startInstant = parseTime(year, month, day, startTime, timeZone)
            val endInstant = parseTime(year, month, day, endTime, timeZone)

            addEvent(
                title = title,
                description = description,
                startingTime = startInstant,
                endingTime = endInstant,
                priority = priority
            ) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun parseTime(year: String, month: String, day: String, time: String, timeZone: TimeZone): Instant? {
        if (time.isEmpty()) return null
        return try {
            val normalizedTime = if (time.count { it == ':' } == 1) "$time:00" else time
            LocalDateTime.parse(
                "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T$normalizedTime"
            ).toInstant(timeZone)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Removes an event from the calendar by its [id].
     *
     * @param id The unique identifier of the event to remove.
     * @return True if successful, false otherwise.
     */
    suspend fun removeEvent(id: String): Boolean {
        return try {
            val event = repository.get(id)
            event?.linkedTasks?.forEach { taskId ->
                tasksRepository.delete(taskId)
            }
            repository.delete(id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete event", e)
            false
        }
    }

    /**
     * Updates an existing [event].
     *
     * @param event The updated event item.
     * @return True if successful, false otherwise.
     */
    suspend fun updateEvent(event: CalendarItem): Boolean {
        return try {
            repository.updateCloud(event)

            // Sync with linked tasks
            event.linkedTasks.forEach { taskId ->
                tasksRepository.get(taskId)?.let { task ->
                    val updatedTask = task.copy(
                        modified = event.modified,
                        title = event.title,
                        description = event.description,
                        startingTime = event.startingTime,
                        endingTime = event.endingTime,
                        priority = event.priority,
                        repeat = event.repeat
                    )
                    tasksRepository.updateCloud(updatedTask)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update event", e)
            false
        }
    }

    /**
     * Retrieves a single event by its [id].
     */
    suspend fun getEvent(id: String): CalendarItem? {
        return try {
            repository.get(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve event", e)
            null
        }
    }

    /**
     * Retrieves all events stored in the repository.
     */
    suspend fun getAllEvents(): List<CalendarItem> {
        return try {
            repository.getAll()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve events", e)
            emptyList()
        }
    }

    /**
     * Synchronizes the local repository with the cloud database.
     */
    suspend fun sync(): Boolean {
        return try {
            repository.sync()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synchronize calendar", e)
            false
        }
    }

    /**
     * Helper to add a task, used for linked creation.
     */
    suspend fun addTask(task: TaskItem) {
        tasksRepository.insert(task)
    }

    /* --------------------------
     * Date Queries
     * -------------------------- */

    /**
     * Filters all events occurring on a specific [date].
     */
    suspend fun getEventsOn(date: LocalDate): List<CalendarItem> {
        return try {
            repository.getAll().filter {
                it.startingTime
                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                    ?.date == date
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query events for date", e)
            emptyList()
        }
    }

    /**
     * Retrieves events scheduled for today.
     * Note: Current implementation returns an empty list (Placeholder).
     */
    suspend fun getToday(): List<CalendarItem> {
        return try {
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve today's events", e)
            emptyList()
        }
    }

    /**
     * Retrieves all upcoming events, sorted by their start time.
     */
    suspend fun getUpcomingEvents(): List<CalendarItem> {
        return try {
            repository.getAll()
                .sortedBy {
                    it.startingTime ?: Instant.fromEpochMilliseconds(0)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve upcoming events", e)
            emptyList()
        }
    }

    /* --------------------------
     * Helper Methods
     * -------------------------- */

    /**
     * Checks if there are any events scheduled for a given [date].
     */
    suspend fun hasEvents(date: LocalDate): Boolean {
        return try {
            getEventsOn(date).isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Failed checking events", e)
            false
        }
    }

    /**
     * Returns the total count of events in the calendar.
     */
    suspend fun eventCount(): Int {
        return try {
            repository.getAll().size
        } catch (e: Exception) {
            Log.e(TAG, "Failed counting events", e)
            0
        }
    }
}
