package com.remmi.app.plugins.calendar

import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import android.util.Log
import com.remmi.app.core.events.MeetingCreatedEvent
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.widgets.RemmiWidget

class CalendarPlugin : RemmiPlugin {

    override val metadata = PluginMetadata(
        id = "calendar",
        name = "Calendar",
        version = "1.0.0",
        author = "Mark"
    )

    override fun onLoad(context: PluginContext) {
        Log.d("Remmi", "Calendar plugin loaded")

        context.eventBus.publish(
            MeetingCreatedEvent()
        )
    }

    override fun onUnload() {
        println("Calendar unloaded")
    }

    override fun getWidget(): RemmiWidget {
        return CalendarWidget()
    }
}