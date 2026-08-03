package com.remmi.app.core.plugins

import com.remmi.app.plugins.calendar.CalendarPlugin

class PluginLoader {
    fun loadCorePlugins() : List<RemmiPlugin> {

        return listOf(CalendarPlugin())
    }
}