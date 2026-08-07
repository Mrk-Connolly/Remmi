package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.DatabaseService
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.widgets.RemmiWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Alarm plugin.
 *
 * Integrates alarm scheduling and management into the Remmi platform.
 */
class AlarmPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    /**
     * Dashboard widget for alarms.
     */
    override val widget: RemmiWidget = AlarmWidget()

    /**
     * Repository for persistent alarm data.
     */
    override val repository: AlarmRepository = AlarmRepository(SupabaseService)

    /**
     * Action controller for alarm logic.
     */
    override val actions: AlarmActions = AlarmActions(repository)

    /**
     * UI screen for detailed alarm management.
     */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = AlarmScreen(actions)
    }

    /**
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "Loading Alarm Plugin...")
    }

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
    }

    /**
     * Triggers a sync of alarms from the cloud.
     */
    override fun loadItems(service: DatabaseService) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync alarms: ${e.message}")
            }
        }
    }
}
