package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.CreateAlarmCommand
import com.remmi.app.core.events.commands.DeleteAlarmCommand
import com.remmi.app.core.events.commands.DeleteDataCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.commands.UpdateAlarmCommand
import com.remmi.app.core.events.commands.UpsertDataCommand
import com.remmi.app.core.events.events.AlarmCreatedEvent
import com.remmi.app.core.events.events.AlarmDeletedEvent
import com.remmi.app.core.events.events.AlarmUpdatedEvent
import com.remmi.app.core.events.events.CalendarEventDeletedEvent
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.alarm.ui.screens.AlarmScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Alarm plugin.
 *
 * Integrates alarm scheduling and management into the Remmi platform.
 */
class AlarmPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: AlarmRepository? = null
    private var _actions: AlarmActions? = null

    /** Repository for persistent alarm data. */
    override val repository: AlarmRepository
        get() = _repository ?: throw IllegalStateException("AlarmPlugin not initialized")

    /** Action controller for alarm logic. */
    override val actions: AlarmActions
        get() = _actions ?: throw IllegalStateException("AlarmPlugin not initialized")

    /** Dashboard widget for alarms. */
    override val widget: RemmiWidget by lazy { AlarmWidget(metadata, actions) }

    /** UI screen for detailed alarm management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[AlarmPlugin] - [Content] executed")
            AlarmScreen(actions, controller)
        }
    }


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
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[AlarmPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = AlarmRepository(context.databaseManager.service)
        _repository = repo
        
        // Initialize Actions
        _actions = AlarmActions(repo).apply {
            this.eventBus = context.eventBus
            this.alarmService = context.androidManager.alarmService
        }
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Alarm plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[AlarmPlugin] - Received command: ${command::class.simpleName}")
        when (command) {
            is CreateAlarmCommand -> {
                val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                val alarmId = java.util.UUID.randomUUID().toString()
                val item = AlarmItem(
                    id = alarmId,
                    created = now,
                    modified = now,
                    title = command.title,
                    description = command.description,
                    time = command.time,
                    isPriority = command.isPriority,
                    repeatable = command.repeatable,
                    custom = command.custom,
                    useSound = command.useSound,
                    useVibration = command.useVibration,
                    linkedCalendarEvent = if (command.source == "calendar") "event_key" else null, // Placeholder
                    userId = null
                )
                
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "alarms",
                        item = item,
                        serializer = AlarmItem.serializer(),
                        source = "alarm"
                    )
                )
                
                // If syncToSystem is true, also notify the Android system via AlarmService
                if (command.syncToSystem) {
                    actions.alarmService?.setAlarm(item.id, item.title, item.time.toEpochMilliseconds(), item.useSound, item.useVibration)
                    actions.alarmService?.syncToSystemClock(item.title, item.time.toEpochMilliseconds())
                }

                actions.eventBus?.publishEvent(
                    AlarmCreatedEvent(alarmId = item.id)
                )
            }
            is UpdateAlarmCommand -> {
                actions.eventBus?.publishCommand(
                    UpsertDataCommand(
                        tableName = "alarms",
                        item = command.alarm,
                        serializer = AlarmItem.serializer(),
                        source = "alarm"
                    )
                )
                actions.eventBus?.publishEvent(
                    AlarmUpdatedEvent(alarmId = command.alarm.id)
                )
            }
            is DeleteAlarmCommand -> {
                actions.eventBus?.publishCommand(
                    DeleteDataCommand(
                        tableName = "alarms",
                        itemId = command.alarmId,
                        source = "alarm"
                    )
                )
                actions.eventBus?.publishEvent(
                    AlarmDeletedEvent(alarmId = command.alarmId)
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
            is CalendarEventDeletedEvent -> {
                Log.i("Remmi", "[AlarmPlugin] - Calendar event ${event.itemId} deleted. Cleaning up linked alarms...")
                val allAlarms = actions.getAllAlarms()
                val linkedAlarms = allAlarms.filter { it.alarm.linkedCalendarEvent == event.itemId }
                linkedAlarms.forEach { alarm ->
                    actions.eventBus?.publishCommand(
                        DeleteAlarmCommand(alarmId = alarm.alarm.id, source = "alarm_cleanup")
                    )
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
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync alarms: ${e.message}")
            }
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
        _repository?.clear()
    }
}
