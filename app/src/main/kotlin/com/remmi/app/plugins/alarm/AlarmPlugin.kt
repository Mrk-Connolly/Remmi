package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.plugins.widgets.RemmiWidget
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
    override val metadata: PluginMetadata,
    private val pluginManager: PluginManager
) : RemmiPlugin {

    init {
        Log.d("Remmi", "[AlarmPlugin] - [constructor] executed")
    }

    /**
     * Repository for persistent alarm data.
     */
    override val repository: AlarmRepository = AlarmRepository(SupabaseService)

    /**
     * Action controller for alarm logic.
     */
    override val actions: AlarmActions = AlarmActions(repository, pluginManager)

    /**
     * Dashboard widget for alarms.
     */
    override val widget: RemmiWidget = AlarmWidget(metadata, actions)

    /**
     * UI screen for detailed alarm management.
     */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() {
            Log.d("Remmi", "[AlarmPlugin] - [Content] executed")
            AlarmScreen(actions)
        }
    }

    /**
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

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[AlarmPlugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        Log.d("Remmi", "[AlarmPlugin] - [reformat] executed")
        repository.clearAll()
    }
}
