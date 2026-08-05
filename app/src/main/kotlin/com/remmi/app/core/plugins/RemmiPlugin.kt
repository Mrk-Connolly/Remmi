package com.remmi.app.core.plugins

import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.widgets.RemmiWidget
import com.remmi.app.core.actions.RemmiAction
interface RemmiPlugin {

    val metadata : PluginMetadata
    val screen : RemmiScreen
    val widget : RemmiWidget


    fun onLoad(context: PluginContext)

    fun onUnload()

}