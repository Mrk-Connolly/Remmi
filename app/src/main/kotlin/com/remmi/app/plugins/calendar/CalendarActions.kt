package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.tasks.TaskItem
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.TasksRepository
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Calendar plugin.
 */
class CalendarActions(
    private val repository: CalendarRepository,
    private val tasksRepository: TasksRepository,
    private val pluginManager: PluginManager,
    override val id: String,
    override val name: String
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Calendar Actions
     * */
    init {
        Log.d("Remmi", "[CalendarActions] - Constructor initialized")
    }

    companion object {
        private const val TAG = "CalendarActions"
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Get Alarm Actions
     * Retrieve actions from the Alarm plugin
     * */
    fun getAlarmActions(): AlarmActions? {
        Log.d("Remmi", "[CalendarActions] - [getAlarmActions] executed")
        return pluginManager.plugins["alarm"]?.actions as? AlarmActions
    }

    /**                                 Get Contact Actions
     * Retrieve actions from the Contacts plugin
     * */
    fun getContactActions(): ContactActions? {
        Log.d("Remmi", "[CalendarActions] - [getContactActions] executed")
        return pluginManager.plugins["contacts"]?.actions as? ContactActions
    }

    /**                                 Get Tasks Actions
     * Retrieve actions from the Tasks plugin
     * */
    fun getTasksActions(): TasksActions? {
        Log.d("Remmi", "[CalendarActions] - [getTasksActions] executed")
        return pluginManager.plugins["tasks"]?.actions as? TasksActions
    }

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
            item.id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert event", e)
            null
        }
    }

    /**                                 Remove Event
     * Delete an event and its linked tasks
     * */
    suspend fun removeEvent(id: String): Boolean {
        Log.d("Remmi", "[CalendarActions] - [removeEvent] executed")
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

    /**                                 Update Event
     * Update event details and synchronize linked tasks
     * */
    suspend fun updateEvent(event: CalendarItem): Boolean {
        Log.d("Remmi", "[CalendarActions] - [updateEvent] executed")
        return try {
            event.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(event)

            // Sync with linked tasks
            event.linkedTasks.forEach { taskId ->
                tasksRepository.get(taskId)?.let { task ->
                    val updatedTask = task.copy(
                        modified = event.modified,
                        title = event.title,
                        description = event.description,
                        dueDate = event.startingDate.atTime(event.startingTime ?: LocalTime(0, 0)).toInstant(TimeZone.currentSystemDefault()),
                        isPriority = event.isPriority,
                        group = event.group
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

    /**                                 Add Task
     * Create and insert a new task associated with the calendar
     * */
    suspend fun addTask(task: TaskItem) {
        Log.d("Remmi", "[CalendarActions] - [addTask] executed")
        tasksRepository.insert(task)
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
     * Retrieve all unique group names used in events and tasks
     * */
    suspend fun getAllGroups(): List<String> {
        Log.d("Remmi", "[CalendarActions] - [getAllGroups] executed")
        val eventGroups = repository.getAll().mapNotNull { it.group }
        val taskGroups = tasksRepository.getAll().mapNotNull { it.group }
        return (eventGroups + taskGroups).distinct().sorted()
    }
}
