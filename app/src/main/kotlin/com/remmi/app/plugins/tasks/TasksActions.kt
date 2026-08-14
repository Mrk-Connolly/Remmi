package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.core.plugins.model.components.RepeatRule
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

    init {
        Log.d("Remmi", "[TasksActions] - [constructor] executed")
    }

    companion object {
        private const val TAG = "TasksActions"
    }

    
    fun getAlarmActions(): AlarmActions? {
        Log.d("Remmi", "[TasksActions] - [getAlarmActions] executed")
        return pluginManager.plugins["alarm"]?.actions as? AlarmActions
    }

    suspend fun createTask(
        title: String,
        description: String,
        dueDate: Instant? = null,
        isPriority: Boolean = false,
        group: String? = null,
        repeat: RepeatRule? = null,
        addToCalendar: Boolean = false,
        addToAlarm: Boolean = false,
        alarmTime: Instant? = null
    ): Boolean {
        Log.d("Remmi", "[TasksActions] - [createTask] executed")
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
                    isPriority = isPriority,
                    group = group,
                    linkedTasks = listOf(taskId)
                )
                calendarRepository.insert(calendarItem)
                calendarItemId = calendarItem.id
            }
            
            if (addToAlarm) {
                val finalAlarmTime = alarmTime ?: dueDate
                if (finalAlarmTime != null) {
                    getAlarmActions()?.addAlarm(
                        title = title,
                        description = description,
                        time = finalAlarmTime,
                        isPriority = isPriority
                    )
                }
            }

            val task = TaskItem(
                id = taskId,
                created = now,
                modified = now,
                title = title,
                description = description,
                dueDate = dueDate,
                isPriority = isPriority,
                group = group,
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
        Log.d("Remmi", "[TasksActions] - [updateTask] executed")
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
                        isPriority = task.isPriority,
                        group = task.group
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
        Log.d("Remmi", "[TasksActions] - [deleteTask] executed")
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
        Log.d("Remmi", "[TasksActions] - [toggleTask] executed")
        val updatedTask = task.copy(completed = !task.completed)
        return updateTask(updatedTask)
    }

    suspend fun getAllTasks(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getAllTasks] executed")
        return try {
            repository.getAll().sortedByDescending { it.created }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve tasks", e)
            emptyList()
        }
    }

    suspend fun sync() {
        Log.d("Remmi", "[TasksActions] - [sync] executed")
        try {
            repository.sync()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }

    suspend fun getTodayTasks(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getTodayTasks] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return repository.getAll().filter { 
            (!it.completed) && (it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today)
        }
    }

    suspend fun getHighPriorityTasksOfMonth(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getHighPriorityTasksOfMonth] executed")
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault())
        return repository.getAll().filter { 
            it.isPriority && 
            it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.let { date ->
                date.monthNumber == now.monthNumber && date.year == now.year
            } == true
        }
    }

    suspend fun getAllGroups(): List<String> {
        Log.d("Remmi", "[TasksActions] - [getAllGroups] executed")
        val taskGroups = repository.getAll().mapNotNull { it.group }
        val eventGroups = calendarRepository.getAll().mapNotNull { it.group }
        return (taskGroups + eventGroups).distinct().sorted()
    }
}
