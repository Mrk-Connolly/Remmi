package com.remmi.app.plugins.tasks

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
 * Entry point for the Tasks plugin.
 *
 * Configures the task management components and integrates them into the
 * Remmi system.
 */
class TasksPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    /**
     * Dashboard widget for quick task overview.
     */
    override val widget: RemmiWidget = TasksWidget()

    /**
     * Repository for task data persistence.
     */
    override val repository: TasksRepository = TasksRepository(SupabaseService)

    /**
     * Action controller for task business logic.
     */
    override val actions: TasksActions = TasksActions(repository)

    /**
     * Main UI screen for detailed task management.
     */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = TasksScreen(actions)
    }

    /**
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "Loading Tasks Plugin...")
    }

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
    }

    /**
     * Syncs task items from the cloud service.
     */
    override fun loadItems(service: DatabaseService) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync tasks: ${e.message}")
            }
        }
    }
}
