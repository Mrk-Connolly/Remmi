package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.ui.screens.TasksScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

/**
 * Entry point for the Tasks plugin.
 */
class TasksPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: TasksRepository = TasksRepository()
    private val _actions: TasksActions = TasksActions(_repository).apply {
        this.eventBus = this@TasksPlugin.eventBus
    }

    /** Repository for managing Tasks data */
    override val repository: TasksRepository get() = _repository

    /** Action controller for tasks logic. */
    override val actions: TasksActions get() = _actions

    /** Dashboard widget for tasks. */
    override val widget: RemmiWidget by lazy { TasksWidget(metadata, actions) }

    /** UI screen for task management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[TasksPlugin] - [Content] executed")
            TasksScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[TasksPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[TasksPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Tasks plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[TasksPlugin] - Received command: ${command::class.simpleName}")
        when (command) {
            is CreateTaskCommand -> {
                actions.createTask(
                    title = command.title,
                    description = command.description,
                    dueDate = command.dueDate,
                    isPriority = command.isPriority,
                    group = command.group,
                    repeat = command.repeat,
                    sourcePlugin = command.sourcePlugin,
                    sourceItemId = command.sourceItemId,
                    correlationId = command.correlationId ?: command.commandId,
                    causationId = command.commandId,
                    creationContext = command.creationContext ?: CreationContext.PRIMARY
                )
            }
            is UpdateTaskCommand -> {
                actions.updateTask(command.task)
            }
            is com.remmi.app.core.eventBus.commands.DeleteTaskCommand -> {
                actions.deleteTask(
                    id = command.taskId,
                    correlationId = command.correlationId ?: command.commandId,
                    causationId = command.commandId,
                    deletionContext = command.deletionContext ?: DeletionContext.PRIMARY
                )
            }
            is com.remmi.app.core.eventBus.commands.ToggleTaskCommand -> {
                Log.d("Remmi", "[TasksPlugin] - Toggling task: ${command.taskId}")
                val task = actions.getTask(command.taskId)
                task?.let { actions.toggleTask(it) }
            }
            is FetchTodayTasksCommand -> {
                Log.d("Remmi", "[TasksPlugin] - Fetching today's tasks for automation")
                val tasks = actions.getTodayTasks()
                actions.eventBus?.publishEvent(TodayTasksFetchedEvent(tasks))
            }
            
            is FetchWeeklyTasksCommand -> {
                Log.d("Remmi", "[TasksPlugin] - Fetching weekly tasks for lock screen")
                val tasks = actions.getWeeklyTasks()
                actions.eventBus?.publishEvent(WeeklyTasksFetchedEvent(tasks))
            }
        }
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[TasksPlugin] - Received event: ${event::class.simpleName}")
        when (event) {
            is CalendarEventCreatedEvent -> {
                if (event.linkedRequests.createTask) {
                    Log.i("Remmi", "[TasksPlugin] - Calendar event requested task. Requesting calendar item...")
                    eventBus.publishCommand(
                        FetchDataByIdCommand(
                            tableName = "calendar",
                            itemId = event.itemId,
                            serializer = CalendarItem.serializer(),
                            correlationId = event.correlationId ?: event.eventId,
                            causationId = event.eventId,
                            source = "tasks_plugin"
                        )
                    )
                }
            }
            is CalendarEventDeletedEvent -> {
                Log.i("Remmi", "[TasksPlugin] - Source calendar event ${event.itemId} deleted. Cleaning up linked tasks...")
                eventBus.publishCommand(
                    FetchDataBySourceCommand(
                        tableName = "tasks",
                        sourcePlugin = "calendar",
                        sourceItemId = event.itemId,
                        serializer = TaskItem.serializer(),
                        correlationId = "tasks_plugin_cleanup_${event.itemId}",
                        causationId = event.eventId,
                        source = "tasks_plugin"
                    )
                )
            }
            is DataFetchedEvent<*> -> {
                handleDataFetched(event)
            }
        }
    }

    private suspend fun handleDataFetched(event: DataFetchedEvent<*>) {
        if (event.source == "database" && event.items.isNotEmpty()) {
            val firstItem = event.items[0]
            if (firstItem is CalendarItem) {
                Log.d("Remmi", "[TasksPlugin] - Received calendar item for linked task creation")
                val dueDate = firstItem.startingTime?.let { time ->
                    firstItem.startingDate.atTime(time).toInstant(TimeZone.currentSystemDefault())
                } ?: firstItem.startingDate.atTime(0, 0).toInstant(TimeZone.currentSystemDefault())
                
                actions.eventBus?.publishCommand(
                    CreateTaskCommand(
                        title = "Task for: ${firstItem.title}",
                        description = firstItem.description,
                        dueDate = dueDate,
                        isPriority = firstItem.isPriority,
                        group = firstItem.group,
                        sourcePlugin = "calendar",
                        sourceItemId = firstItem.id,
                        correlationId = event.correlationId,
                        causationId = event.eventId,
                        creationContext = CreationContext.SECONDARY_LINKED,
                        source = "tasks_plugin"
                    )
                )
            }
            else if (firstItem is TaskItem) {
                if (event.correlationId?.startsWith("tasks_plugin_cleanup") == true) {
                    event.items.forEach { task ->
                        if (task is TaskItem) {
                            actions.eventBus?.publishCommand(
                                DeleteTaskCommand(
                                    taskId = task.id,
                                    source = "tasks_cleanup",
                                    correlationId = event.correlationId,
                                    causationId = event.eventId,
                                    deletionContext = DeletionContext.LINKED_CLEANUP
                                )
                            )
                        }
                    }
                } else {
                    // Global sync or fetch
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<TaskItem>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[TasksPlugin] - Updated repository with ${event.items.size} tasks")
                }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[TasksPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Tasks Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Tasks Plugin Loaded")
    }

    /**                                   Refresh
     * Sync tasks with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[TasksPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync tasks: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[TasksPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[TasksPlugin] - [reformat] executed")
        _repository.clear()
    }
}
