package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.widgets.RemmiWidget

class CalendarPlugin(
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = CalendarScreen()
    },
    override val widget: RemmiWidget = CalendarWidget()
) : RemmiPlugin {

    override val metadata = PluginMetadata(
        id = "calendar",
        name = "Calendar",
        version = "1.0.0",
        author = "Mark",
        enabled = true
    )

    lateinit var repository: CalendarRepository
        private set

    lateinit var actions: CalendarActions
        private set

    override fun onLoad(context: PluginContext) {

        Log.d("Remmi", "Loading Calendar Plugin...")

        repository = CalendarRepository()

        actions = CalendarActions(repository)

        Log.d(
            "Remmi",
            "Calendar loaded with ${actions.eventCount()} events."
        )
    }

    override fun onUnload() {
        Log.d("Remmi", "Calendar Plugin unloaded.")
    }

}