package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.plugins.calendar.CalendarRepository
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
     * Repository for task data persistence.
     */
    override val repository: TasksRepository = TasksRepository(SupabaseService)

    /**
     * Action controller for task business logic.
     */
    override val actions: TasksActions = TasksActions(
        repository,
        CalendarRepository(SupabaseService)
    )

    /**
     * Dashboard widget for quick task overview.
     */
    override val widget: RemmiWidget = TasksWidget(actions)

    /**
     * Main UI screen for detailed task management.
     */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = TasksScreen(actions)
    }

    /**
     * Load plugin and items.
     */
    override fun onLoad() {
        Log.d("Remmi", "Loading Tasks Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
    }

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
    }
}
