package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.components.Priority
import com.remmi.app.plugins.tasks.TaskItem
import com.remmi.app.plugins.tasks.TasksRepository
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Calendar plugin.
 */
class CalendarActions(
    private val repository: CalendarRepository,
    private val tasksRepository: TasksRepository,
    override val id: String,
    override val name: String
) : RemmiAction {

    companion object {
        private const val TAG = "CalendarActions"
    }

    suspend fun addEvent(
        id: String = UUID.randomUUID().toString(),
        title: String,
        description: String,
        startingDate: LocalDate,
        startingTime: LocalTime? = null,
        endingDate: LocalDate? = null,
        endingTime: LocalTime? = null,
        priority: Priority = Priority.Normal,
        participants: List<String> = emptyList(),
        repeat: List<String> = emptyList(),
        location: List<String> = emptyList(),
        linkedTasks: List<String> = emptyList(),
        linkedAlarm: String? = null
    ): String? {
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
                priority = priority,
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

    suspend fun updateEvent(event: CalendarItem): Boolean {
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
                        priority = event.priority
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

    suspend fun getAllEvents(): List<CalendarItem> {
        return try {
            repository.getAll()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve events", e)
            emptyList()
        }
    }

    suspend fun sync(): Boolean {
        return try {
            repository.sync()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synchronize calendar", e)
            false
        }
    }

    suspend fun addTask(task: TaskItem) {
        tasksRepository.insert(task)
    }

    suspend fun getEventsOn(date: LocalDate): List<CalendarItem> {
        return try {
            repository.getAll().filter { it.startingDate == date }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query events for date", e)
            emptyList()
        }
    }

    suspend fun getUpcomingEvents(): List<CalendarItem> {
        return try {
            repository.getAll()
                .sortedBy { it.startingDate }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve upcoming events", e)
            emptyList()
        }
    }
}
