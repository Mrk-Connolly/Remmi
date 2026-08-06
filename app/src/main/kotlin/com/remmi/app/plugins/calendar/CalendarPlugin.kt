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

class CalendarPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    override val widget: RemmiWidget = CalendarWidget()


    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = CalendarScreen(actions)
    }


    // On LOAD FUNCTION
    override fun onLoad() {
        Log.d("Remmi", "Loading Calendar Plugin...")

        repository = CalendarRepository(SupabaseService)
        actions = CalendarActions(repository)

        Log.d(
            "Remmi",
            "Calendar loaded with ${repository.getAll().size} events."
        )
    }


    // ON UNLOAD FUNCTION
    override fun onUnload() {
        TODO("Not yet implemented")
    }



    lateinit var repository: CalendarRepository
        private set

    lateinit var actions: CalendarActions
        private set

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