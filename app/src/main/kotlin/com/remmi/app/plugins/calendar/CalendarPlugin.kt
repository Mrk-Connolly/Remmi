package com.remmi.app.plugins.calendar

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
 * The main entry point for the Calendar plugin.
 *
 * This class coordinates the setup of the repository, action controller, and UI
 * components (Widget and Screen). It also handles the background synchronization
 * of calendar items.
 */
class CalendarPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    /**
     * The dashboard widget for displaying calendar information.
     */
    override val widget: RemmiWidget = CalendarWidget()

    /**
     * The repository that manages persistent calendar data.
     */
    override val repository: CalendarRepository = CalendarRepository(SupabaseService)

    /**
     * The action controller that manages calendar business logic.
     */
    override val actions: CalendarActions = CalendarActions(repository)

    /**
     * The main full-screen UI for the calendar.
     */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = CalendarScreen(actions)
    }

    /**
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "Loading Calendar Plugin...")

        Log.d(
            "Remmi",
            "Calendar loaded with ${repository.getAll().size} events."
        )
    }

    /**
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        // Implementation for cleanup when the plugin is removed or the app closes.
    }

    /**
     * Triggers a background sync of calendar items from the cloud database.
     */
    override fun loadItems(service: DatabaseService) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Remmi", "Syncing calendar items...")
                actions.sync()
                Log.d("Remmi", "Calendar synced with ${repository.getAll().size} events.")
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync calendar: ${e.message}")
            }
        }
    }
}
