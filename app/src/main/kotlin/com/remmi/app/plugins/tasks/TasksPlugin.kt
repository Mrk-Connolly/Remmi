package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.plugins.calendar.CalendarRepository
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.ui.screens.TasksScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Tasks plugin.
 */
class TasksPlugin(
    override val metadata: PluginMetadata,
    private val pluginManager: PluginManager
) : RemmiPlugin {

    init {
        Log.d("Remmi", "[TasksPlugin] - [constructor] executed")
    }

    override val repository: TasksRepository = TasksRepository(SupabaseService)
    override val actions: TasksActions = TasksActions(
        repository,
        CalendarRepository(SupabaseService),
        pluginManager
    )
    override val widget: RemmiWidget = TasksWidget(actions)
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() {
            Log.d("Remmi", "[TasksPlugin] - [Content] executed")
            TasksScreen(actions)
        }
    }

    override fun onLoad() {
        Log.d("Remmi", "[TasksPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Tasks Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
    }

    override fun onUnload() {
        Log.d("Remmi", "[TasksPlugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        Log.d("Remmi", "[TasksPlugin] - [reformat] executed")
        repository.clearAll()
    }
}
