package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.events.EventType
import com.remmi.app.core.events.PluginEvent
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.core.plugins.model.components.RepeatRule
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Tasks plugin.
 */
class TasksActions(
    private val repository: TasksRepository,
    override val id: String = "tasks_actions",
    override val name: String = "Tasks Actions"
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system event bus */
    override var eventBus: EventBus? = null

    companion object {
        private const val TAG = "TasksActions"
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Tasks Actions
     * */
    init {
        Log.d("Remmi", "[TasksActions] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Create Task
     * Create a new task and publish a Fact event
     * */
    suspend fun createTask(
        title: String,
        description: String,
        dueDate: Instant? = null,
        isPriority: Boolean = false,
        group: String? = null,
        repeat: RepeatRule? = null
    ): Boolean {
        Log.d("Remmi", "[TasksActions] - [createTask] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val taskId = UUID.randomUUID().toString()

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
                linkedCalendar = null // Will be linked via AutomationEngine if needed
            )

            repository.insert(task)
            Log.d(TAG, "Task created successfully")

            // Publish Fact
            Log.i("Remmi", "[TasksActions] - Successfully created task: ${task.id}. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "tasks",
                    type = EventType.CREATED,
                    itemId = task.id
                )
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create task", e)
            false
        }
    }

    /**                                 Update Task
     * Update task details and publish a Fact event
     */
    suspend fun updateTask(task: TaskItem): Boolean {
        Log.d("Remmi", "[TasksActions] - [updateTask] executed")
        return try {
            task.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(task)

            // Publish Fact
            Log.i("Remmi", "[TasksActions] - Successfully updated task: ${task.id}. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "tasks",
                    type = EventType.UPDATED,
                    itemId = task.id
                )
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update task", e)
            false
        }
    }

    /**                                 Delete Task
     * Delete a task by ID and publish a Fact event
     * */
    suspend fun deleteTask(id: String): Boolean {
        Log.d("Remmi", "[TasksActions] - [deleteTask] executed")
        return try {
            repository.delete(id)

            // Publish Fact
            Log.i("Remmi", "[TasksActions] - Successfully deleted task: $id. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "tasks",
                    type = EventType.DELETED,
                    itemId = id
                )
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task", e)
            false
        }
    }

    /**                                 Toggle Task
     * Toggle completion status of a task
     * */
    suspend fun toggleTask(task: TaskItem): Boolean {
        Log.d("Remmi", "[TasksActions] - [toggleTask] executed")
        val updatedTask = task.copy(completed = !task.completed)
        return updateTask(updatedTask)
    }

    /**                                 Get All
     * Retrieve all tasks sorted by creation date
     * */
    suspend fun getAllTasks(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getAllTasks] executed")
        return try {
            repository.getAll().sortedByDescending { it.created }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve tasks", e)
            emptyList()
        }
    }

    /**                                 Sync
     * Synchronize tasks with the cloud
     * */
    suspend fun sync() {
        Log.d("Remmi", "[TasksActions] - [sync] executed")
        try {
            repository.sync()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed")
        }
    }

    /**                                 Get Today
     * Retrieve incomplete tasks due today
     * */
    suspend fun getTodayTasks(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getTodayTasks] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return repository.getAll().filter { 
            (!it.completed) && (it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today)
        }
    }

    /**                                 Get High Priority (Month)
     * Retrieve high priority tasks due in the current month
     * */
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

    /**                                 Get All Groups
     * Retrieve all unique group names from tasks
     * */
    suspend fun getAllGroups(): List<String> {
        Log.d("Remmi", "[TasksActions] - [getAllGroups] executed")
        return repository.getAll().mapNotNull { it.group }.distinct().sorted()
    }
}
