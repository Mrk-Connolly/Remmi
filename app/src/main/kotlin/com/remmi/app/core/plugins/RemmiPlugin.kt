package com.remmi.app.core.plugins

import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.widgets.RemmiWidget
import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.service.DatabaseService

interface RemmiPlugin {

    val metadata : PluginMetadata
    val screen : RemmiScreen
    val widget : RemmiWidget



    fun onLoad()

    fun onUnload()

    fun loadItems(service: DatabaseService)
}