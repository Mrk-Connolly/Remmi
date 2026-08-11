package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main entry point for the Calendar plugin.
 */
class CalendarPlugin(
    override val metadata: PluginMetadata,
    private val pluginManager: PluginManager
) : RemmiPlugin {

    override val repository: CalendarRepository = CalendarRepository(SupabaseService)
    override val actions: CalendarActions = CalendarActions(
        repository,
        TasksRepository(SupabaseService),
        pluginManager,
        id = "calendar_actions",
        name = "Calendar Actions"
    )
    override val widget: RemmiWidget = CalendarWidget(actions, pluginManager)
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content() = CalendarScreen(actions)
    }

    override fun onLoad() {
        Log.d("Remmi", "Loading Calendar Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
        Log.d("Remmi", "Calendar Plugin Loaded")
    }

    override fun onUnload() {
        Log.d("Remmi", "Unloading Calendar Plugin...")
    }
}
