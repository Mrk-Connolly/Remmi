package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.events.TaskCreatedEvent
import com.remmi.app.core.eventBus.events.TaskDeletedEvent
import com.remmi.app.core.eventBus.events.TaskUpdatedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.plugins.tasks.models.TaskItem
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
        repeat: RepeatRule? = null,
        sourcePlugin: String? = null,
        sourceItemId: String? = null,
        correlationId: String? = null,
        causationId: String? = null,
        creationContext: CreationContext? = null
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
                sourcePlugin = sourcePlugin,
                sourceItemId = sourceItemId
            )

            repository.insert(task)
            Log.d(TAG, "Task created successfully")

            // Publish Fact
            Log.i("Remmi", "[TasksActions] - Successfully created task: ${task.id}. Publishing event...")
            eventBus?.publishEvent(
                TaskCreatedEvent(
                    taskId = task.id,
                    priority = task.isPriority,
                    group = task.group,
                    correlationId = correlationId,
                    causationId = causationId,
                    creationContext = creationContext
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
                TaskUpdatedEvent(taskId = task.id)
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
    suspend fun deleteTask(
        id: String,
        correlationId: String? = null,
        causationId: String? = null,
        deletionContext: DeletionContext? = null
    ): Boolean {
        Log.d("Remmi", "[TasksActions] - [deleteTask] executed")
        return try {
            repository.delete(id)

            // Publish Fact
            Log.i("Remmi", "[TasksActions] - Successfully deleted task: $id. Publishing event...")
            eventBus?.publishEvent(
                TaskDeletedEvent(
                    taskId = id,
                    correlationId = correlationId,
                    causationId = causationId,
                    deletionContext = deletionContext
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

    /**                                 Cleanup Finished
     * Remove tasks that were completed before today.
     * */
    suspend fun cleanupOldFinishedTasks() {
        Log.d("Remmi", "[TasksActions] - [cleanupOldFinishedTasks] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val tasksToDelete = repository.getAll().filter { task ->
            task.completed && task.modified.toLocalDateTime(TimeZone.currentSystemDefault()).date < today
        }
        
        tasksToDelete.forEach { task ->
            Log.d(TAG, "Cleaning up old finished task: ${task.id}")
            repository.delete(task.id)
            eventBus?.publishEvent(TaskDeletedEvent(taskId = task.id, source = "tasks_cleanup"))
        }
    }

    /**                                 Get Task
     * Retrieve a specific task by ID
     * */
    suspend fun getTask(id: String): TaskItem? {
        Log.d("Remmi", "[TasksActions] - [getTask] executed")
        return repository.get(id)
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

    /**                                 Get Weekly
     * Retrieve incomplete tasks due in the next 7 days
     * */
    suspend fun getWeeklyTasks(): List<TaskItem> {
        Log.d("Remmi", "[TasksActions] - [getWeeklyTasks] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val nextWeek = today.plus(7, DateTimeUnit.DAY)
        return repository.getAll().filter { 
            (!it.completed) && it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date?.let { date ->
                date in today..nextWeek
            } == true
        }.sortedBy { it.dueDate }
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
