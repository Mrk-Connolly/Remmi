package com.remmi.app.plugins.calendar

import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin

class CalendarPlugin : RemmiPlugin {

    override val metadata = PluginMetadata(
        id = "calendar",
        name = "Calendar",
        version = "1.0.0",
        author = "Mark"
    )

    override fun onLoad() {
        println("Calendar loaded")
    }

    override fun onUnload() {
        println("Calendar unloaded")
    }
}