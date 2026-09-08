package com.remmi.app.plugins.alarm

import android.content.Context
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
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.plugins.alarm.ui.screens.AlarmScreen
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.LinkedCreationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Alarm plugin.
 *
 * Integrates alarm scheduling and management into the Remmi platform.
 */
class AlarmPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus,
    private val context: Context
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    private val _actions: AlarmActions = AlarmActions(context).apply {
        this.eventBus = this@AlarmPlugin.eventBus
    }

    /** Action controller for alarm logic. */
    override val actions: AlarmActions get() = _actions

    /** Repository is no longer used for this plugin persistence, but required by interface */
    private val _dummyRepository = object : MemoryRepository<AlarmItem>() {}
    override val repository: RemmiRepository<out RemmiModel> get() = _dummyRepository

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
        // No longer listening to database events as alarms are local/system only
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[AlarmPlugin] - [onLoad] executed")
    }

    /**                                   Refresh
     * No-op as we don't sync with database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[AlarmPlugin] - Refreshing skipped (system only)")
    }

    /**                                   On Unload
     * Called when the plugin is being unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[AlarmPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[AlarmPlugin] - [reformat] executed")
        _dummyRepository.clear()
    }
}
