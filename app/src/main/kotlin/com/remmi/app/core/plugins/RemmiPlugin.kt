package com.remmi.app.core.plugins

import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.widgets.RemmiWidget

interface RemmiPlugin {

    val metadata : PluginMetadata

    fun onLoad(context: PluginContext)

    fun onUnload()

    fun getWidget(): RemmiWidget? {
        return null
    }

    fun getScreen(): RemmiScreen? {
        return null
    }
}