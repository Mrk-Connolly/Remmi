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
 */
class TasksPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {

    override val repository: TasksRepository = TasksRepository(SupabaseService)
    override val actions: TasksActions = TasksActions(
        repository,
        CalendarRepository(SupabaseService)
    )
    override val widget: RemmiWidget = TasksWidget(actions)
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = TasksScreen(actions)
    }

    override fun onLoad() {
        Log.d("Remmi", "Loading Tasks Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
    }

    override fun onUnload() {
    }
}
