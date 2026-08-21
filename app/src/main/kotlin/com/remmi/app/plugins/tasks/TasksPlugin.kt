package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.*
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.ui.screens.TasksScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Tasks plugin.
 */
class TasksPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: TasksRepository? = null
    private var _actions: TasksActions? = null
    private var _authRepository: com.remmi.app.core.auth.AuthRepository? = null

    /** Repository for managing Tasks data */
    override val repository: TasksRepository
        get() = _repository ?: throw IllegalStateException("TasksPlugin not initialized")

    /** Action controller for tasks logic. */
    override val actions: TasksActions
        get() = _actions ?: throw IllegalStateException("TasksPlugin not initialized")

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
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[TasksPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = TasksRepository(context.serviceManager.databaseService)
        _repository = repo
        _authRepository = context.authRepository
        
        // Initialize Actions
        _actions = TasksActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Tasks plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[TasksPlugin] - Received command: ${command::class.simpleName}")
        when (command) {
            is CreateTaskCommand -> {
                val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                val taskId = java.util.UUID.randomUUID().toString()
                val item = TaskItem(
                    id = taskId,
                    created = now,
                    modified = now,
                    title = command.title,
                    description = command.description,
                    dueDate = command.dueDate,
                    isPriority = command.isPriority,
                    group = command.group,
                    repeat = command.repeat,
                    linkedCalendar = if (command.source == "calendar") "event_key" else null, // TODO: Use real ID if available
                    userId = _authRepository?.getCurrentUserId()
                )
                
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "tasks",
                        item = item,
                        serializer = TaskItem.serializer(),
                        source = "tasks"
                    )
                )
                
                actions.eventBus?.publishEvent(
                    TaskCreatedEvent(
                        taskId = item.id,
                        priority = item.isPriority,
                        group = item.group
                    )
                )
            }
            is UpdateTaskCommand -> {
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "tasks",
                        item = command.task,
                        serializer = TaskItem.serializer(),
                        source = "tasks"
                    )
                )
                actions.eventBus?.publishEvent(
                    TaskUpdatedEvent(taskId = command.task.id)
                )
            }
            is DeleteTaskCommand -> {
                actions.eventBus?.publishCommand(
                    DeleteDataCommand(
                        tableName = "tasks",
                        itemId = command.taskId,
                        source = "tasks"
                    )
                )
                actions.eventBus?.publishEvent(
                    TaskDeletedEvent(taskId = command.taskId)
                )
            }
            is FetchTodayTasksCommand -> {
                Log.d("Remmi", "[TasksPlugin] - Fetching today's tasks for automation")
                val tasks = actions.getTodayTasks()
                actions.eventBus?.publishEvent(TodayTasksFetchedEvent(tasks))
            }
        }
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[TasksPlugin] - Received event: ${event::class.simpleName}")
        when (event) {
            is CalendarEventDeletedEvent -> {
                Log.i("Remmi", "[TasksPlugin] - Calendar event ${event.itemId} deleted. Cleaning up linked tasks...")
                // Find and delete tasks linked to this calendar event
                val linkedTasks = actions.getAllTasks().filter { it.linkedCalendar == event.itemId }
                linkedTasks.forEach { task ->
                    actions.eventBus?.publishCommand(
                        DeleteTaskCommand(taskId = task.id, source = "tasks_cleanup")
                    )
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
            actions.sync()
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
        _repository?.clear()
    }
}
