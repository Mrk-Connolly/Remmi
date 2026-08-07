package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.calendar.CalendarRepository
import kotlinx.datetime.Instant

import java.util.UUID

/**
 * Action controller for the Tasks plugin.
 *
 * Manages task-related logic, including creation, editing, deletion,
 * completion status toggling, and cloud synchronization.
 */
class TasksActions(
    private val repository: TasksRepository,
    private val calendarRepository: CalendarRepository
) : RemmiAction {

    companion object {
        private const val TAG = "TasksActions"
    }

    /**
     * Adds a new task.
     */
    suspend fun addTask(
        title: String,
        description: String,
        startingTime: Instant? = null,
        endingTime: Instant? = null,
        priority: Priority = Priority.NORMAL,
        repeat: RepeatRule? = null,
        addToCalendar: Boolean = false
    ): Boolean {
        return try {
            val taskId = UUID.randomUUID().toString()
            var calendarItemId: String? = null

            if (addToCalendar) {
                val calendarItem = CalendarItem(
                    id = UUID.randomUUID().toString(),
                    created = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    title = title,
                    description = description,
                    startingTime = startingTime,
                    endingTime = endingTime,
                    priority = priority,
                    repeat = repeat,
                    linkedTasks = mutableListOf(taskId)
                )
                calendarRepository.insert(calendarItem)
                calendarItemId = calendarItem.id
            }

            val task = TaskItem(
                id = taskId,
                created = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                title = title,
                description = description,
                startingTime = startingTime,
                endingTime = endingTime,
                priority = priority,
                completed = false,
                repeat = repeat,
                linkedCalendarItem = calendarItemId
            )

            repository.insert(task)
            Log.d(TAG, "Task added successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add task", e)
            false
        }
    }

    /**
     * Updates an existing task.
     */
    suspend fun updateTask(task: TaskItem): Boolean {
        return try {
            task.modified = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            repository.updateCloud(task)

            // Sync with linked calendar item
            task.linkedCalendarItem?.let { calendarId ->
                calendarRepository.get(calendarId)?.let { calendarItem ->
                    val updatedCalendarItem = calendarItem.copy(
                        modified = task.modified,
                        title = task.title,
                        description = task.description,
                        startingTime = task.startingTime,
                        endingTime = task.endingTime,
                        priority = task.priority,
                        repeat = task.repeat
                    )
                    calendarRepository.updateCloud(updatedCalendarItem)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update task", e)
            false
        }
    }

    /**
     * Deletes a task by its [id].
     */
    suspend fun deleteTask(id: String): Boolean {
        return try {
            val task = repository.get(id)
            task?.linkedCalendarItem?.let { calendarId ->
                calendarRepository.delete(calendarId)
            }
            repository.delete(id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task", e)
            false
        }
    }

    /**
     * Toggles the completion status of a task.
     */
    suspend fun toggleTask(task: TaskItem): Boolean {
        val updatedTask = task.copy(completed = !task.completed)
        return updateTask(updatedTask)
    }

    /**
     * Retrieves all tasks currently managed by the plugin.
     */
    suspend fun getAllTasks(): List<TaskItem> {
        return try {
            repository.getAll().sortedByDescending { it.created }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve tasks", e)
            emptyList()
        }
    }

    /**
     * Synchronizes local tasks with the cloud storage.
     */
    suspend fun sync() {
        try {
            repository.sync()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }
}
