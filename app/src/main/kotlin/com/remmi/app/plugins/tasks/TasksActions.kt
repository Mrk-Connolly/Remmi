package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.calendar.CalendarRepository
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Tasks plugin.
 */
class TasksActions(
    private val repository: TasksRepository,
    private val calendarRepository: CalendarRepository,
    private val pluginManager: PluginManager,
    override val id: String = "tasks_actions",
    override val name: String = "Tasks Actions"
) : RemmiAction {

    companion object {
        private const val TAG = "TasksActions"
    }

    fun getAlarmActions(): AlarmActions? = pluginManager.plugins["alarm"]?.actions as? AlarmActions

    suspend fun createTask(
        title: String,
        description: String,
        dueDate: Instant? = null,
        priority: Priority = Priority.Normal,
        repeat: RepeatRule? = null,
        addToCalendar: Boolean = false,
        addToAlarm: Boolean = false
    ): Boolean {
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val taskId = UUID.randomUUID().toString()
            var calendarItemId: String? = null

            if (addToCalendar) {
                val startDateTime = dueDate?.toLocalDateTime(TimeZone.currentSystemDefault()) ?: now.toLocalDateTime(TimeZone.currentSystemDefault())
                val calendarItem = CalendarItem(
                    id = UUID.randomUUID().toString(),
                    created = now,
                    modified = now,
                    title = title,
                    description = description,
                    startingDate = startDateTime.date,
                    startingTime = startDateTime.time,
                    priority = priority,
                    linkedTasks = listOf(taskId)
                )
                calendarRepository.insert(calendarItem)
                calendarItemId = calendarItem.id
            }
            
            if (addToAlarm && dueDate != null) {
                getAlarmActions()?.addAlarm(
                    title = title,
                    description = description,
                    time = dueDate,
                    priority = priority
                )
            }

            val task = TaskItem(
                id = taskId,
                created = now,
                modified = now,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                completed = false,
                repeat = repeat,
                linkedCalendar = calendarItemId
            )

            repository.insert(task)
            Log.d(TAG, "Task created successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create task", e)
            false
        }
    }

    suspend fun updateTask(task: TaskItem): Boolean {
        return try {
            task.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(task)

            // Sync with linked calendar item
            task.linkedCalendar?.let { calendarId ->
                calendarRepository.get(calendarId)?.let { calendarItem ->
                    val startDateTime = task.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())
                    val updatedCalendarItem = calendarItem.copy(
                        modified = task.modified,
                        title = task.title,
                        description = task.description,
                        startingDate = startDateTime?.date ?: calendarItem.startingDate,
                        startingTime = startDateTime?.time ?: calendarItem.startingTime,
                        priority = task.priority
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

    suspend fun deleteTask(id: String): Boolean {
        return try {
            val task = repository.get(id)
            task?.linkedCalendar?.let { calendarId ->
                calendarRepository.delete(calendarId)
            }
            repository.delete(id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task", e)
            false
        }
    }

    suspend fun toggleTask(task: TaskItem): Boolean {
        val updatedTask = task.copy(completed = !task.completed)
        return updateTask(updatedTask)
    }

    suspend fun getAllTasks(): List<TaskItem> {
        return try {
            repository.getAll().sortedByDescending { it.created }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve tasks", e)
            emptyList()
        }
    }

    suspend fun sync() {
        try {
            repository.sync()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }

    suspend fun getTodayTasks(): List<TaskItem> {
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return repository.getAll().filter { 
            (!it.completed) && (it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today)
        }
    }

    suspend fun getHighPriorityTasksOfMonth(): List<TaskItem> {
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
        return repository.getAll().filter { 
            it.priority == Priority.High && 
            it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.let { date ->
                date.monthNumber == now.monthNumber && date.year == now.year
            } == true
        }
    }
}
