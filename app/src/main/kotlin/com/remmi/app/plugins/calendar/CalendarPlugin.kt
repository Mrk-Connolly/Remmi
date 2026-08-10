package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.DatabaseService
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.TasksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main entry point for the Calendar plugin.
 *
 * This class coordinates the setup of the repository, action controller, and UI
 * components (Widget and Screen). It also handles the background synchronization
 * of calendar items.
 */
class CalendarPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    /**
     * Plugin Classes.
     * Created and initialized on class instance creation
     */
    override val widget: RemmiWidget = CalendarWidget()
    override val repository: CalendarRepository = CalendarRepository(SupabaseService)
    override val actions: CalendarActions = CalendarActions(
        repository,
        TasksRepository(SupabaseService),
        id = "calendar_actions",
        name = "Calendar Actions"
    )
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content() = CalendarScreen(actions)
    }


    // --------------------------------------------------------------------------
    //                       REMMI PLUGIN OVERRIDES.
    // --------------------------------------------------------------------------


    /**
     * On load function
     *
     *
     *
     *
     * */
    override fun onLoad() {
        Log.d("Remmi", "Loading Calendar Plugin...")

        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }

        Log.d("Remmi", "Calendar Plugin Loaded")
    }

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "Unloading Calendar Plugin...")
    }

}
