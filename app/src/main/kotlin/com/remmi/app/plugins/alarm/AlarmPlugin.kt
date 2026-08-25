package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.PluginAction
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.android.services.AndroidServiceManager
import com.remmi.app.plugins.alarm.screens.AlarmScreen
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.LinkedCreationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

/**
 * Entry point for the Alarm plugin.
 *
 * Integrates alarm scheduling and management into the Remmi platform.
 */
class AlarmPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val androidManager: AndroidServiceManager,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: AlarmRepository = AlarmRepository(databaseManager.service)
    private val _actions: AlarmActions = AlarmActions(_repository).apply {
        this.eventBus = this@AlarmPlugin.eventBus
        this.alarmService = androidManager.alarmService
    }

    /** Repository for persistent alarm data. */
    override val repository: RemmiRepository<out RemmiModel> get() = _repository

    /** Action controller for alarm logic. */
    override val actions: AlarmActions get() = _actions

    /** Dashboard widget for alarms. */
    override val widget: RemmiWidget by lazy { AlarmWidget(metadata, actions) }

    /** UI screen for detailed alarm management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[AlarmPlugin] - [Content] executed")
            AlarmScreen(actions, controller)
        }
    }

    override val exposedActions: List<PluginAction> = listOf(
        object : PluginAction {
            override val id = "create_alarm"
            override val pluginId = metadata.id
            override val title = "Create Alarm"
            override val icon = Icons.Default.Alarm
            override fun launch() {
                GlobalUIState.pendingAlarmRequest.value = LinkedCreationData(
                    title = "New Alarm",
                    description = "",
                    sourcePlugin = metadata.id,
                    sourceItemId = "manual",
                    correlationId = null,
                    causationId = null
                )
            }
        }
    )


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[AlarmPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[AlarmPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Alarm plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[AlarmPlugin] - Received command: ${command::class.simpleName}")
        when (command) {
            is CreateAlarmCommand -> {
                actions.addAlarm(
                    title = command.title,
                    description = command.description,
                    time = command.time,
                    isPriority = command.isPriority,
                    repeatable = command.repeatable,
                    custom = command.custom,
                    syncToSystem = command.syncToSystem,
                    useSound = command.useSound,
                    useVibration = command.useVibration,
                    sourcePlugin = command.sourcePlugin,
                    sourceItemId = command.sourceItemId,
                    correlationId = command.correlationId ?: command.commandId,
                    causationId = command.commandId,
                    creationContext = command.creationContext ?: CreationContext.PRIMARY
                )
            }
            is UpdateAlarmCommand -> {
                actions.updateAlarm(command.alarm)
            }
            is DeleteAlarmCommand -> {
                actions.deleteAlarm(
                    id = command.alarmId,
                    correlationId = command.correlationId ?: command.commandId,
                    causationId = command.commandId,
                    deletionContext = command.deletionContext ?: DeletionContext.PRIMARY
                )
            }
        }
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[AlarmPlugin] - Received event: ${event::class.simpleName}")
        when (event) {
            is CalendarEventCreatedEvent -> {
                if (event.linkedRequests.createAlarm) {
                    Log.i("Remmi", "[AlarmPlugin] - Calendar event requested alarm. Requesting calendar item...")
                    eventBus.publishCommand(
                        FetchDataByIdCommand(
                            tableName = "calendar",
                            itemId = event.itemId,
                            serializer = CalendarItem.serializer(),
                            correlationId = event.correlationId ?: event.eventId,
                            causationId = event.eventId,
                            source = "alarm_plugin"
                        )
                    )
                }
            }
            is CalendarEventDeletedEvent -> {
                Log.i("Remmi", "[AlarmPlugin] - Source calendar event ${event.itemId} deleted. Cleaning up linked alarms...")
                eventBus.publishCommand(
                    FetchDataBySourceCommand(
                        tableName = "alarms",
                        sourcePlugin = "calendar",
                        sourceItemId = event.itemId,
                        serializer = AlarmItem.serializer(),
                        correlationId = event.correlationId ?: event.eventId,
                        causationId = event.eventId,
                        source = "alarm_plugin_cleanup"
                    )
                )
            }
            is DataFetchedEvent<*> -> {
                handleDataFetched(event)
            }
        }
    }

    private suspend fun handleDataFetched(event: DataFetchedEvent<*>) {
        // Handle Calendar item fetch for linked creation
        if (event.source == "database" && event.items.isNotEmpty()) {
            val item = event.items[0]
            if (item is CalendarItem) {
                Log.d("Remmi", "[AlarmPlugin] - Received calendar item for linked alarm creation")
                if (item.startingTime != null) {
                    val alarmTime = item.startingDate.atTime(item.startingTime).toInstant(TimeZone.currentSystemDefault())
                    actions.eventBus?.publishCommand(
                        CreateAlarmCommand(
                            title = "Alarm: ${item.title}",
                            description = item.description,
                            time = alarmTime,
                            isPriority = item.isPriority,
                            sourcePlugin = "calendar",
                            sourceItemId = item.id,
                            correlationId = event.correlationId,
                            causationId = event.eventId,
                            creationContext = CreationContext.SECONDARY_LINKED,
                            source = "alarm_plugin"
                        )
                    )
                } else {
                    // Missing info, trigger configuration popup
                    GlobalUIState.pendingAlarmRequest.value = LinkedCreationData(
                        title = "Alarm: ${item.title}",
                        description = item.description,
                        sourcePlugin = "calendar",
                        sourceItemId = item.id,
                        correlationId = event.correlationId,
                        causationId = event.eventId
                    )
                }
            }
            // Handle cleanup deletions
            else if (item is AlarmItem && event.causationId?.startsWith("alarm_plugin_cleanup") == true) {
                 event.items.forEach { alarm ->
                     if (alarm is AlarmItem) {
                        actions.eventBus?.publishCommand(
                            DeleteAlarmCommand(
                                alarmId = alarm.id,
                                source = "alarm_cleanup",
                                correlationId = event.correlationId,
                                causationId = event.eventId,
                                deletionContext = DeletionContext.LINKED_CLEANUP
                            )
                        )
                     }
                 }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[AlarmPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Alarm Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Alarm Plugin Loaded")
    }

    /**                                   Refresh
     * Sync alarms with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[AlarmPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync alarms: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[AlarmPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[AlarmPlugin] - [reformat] executed")
        _repository.clear()
    }
}
