package com.remmi.app.core.plugins


interface RemmiPlugin {

    val metadata : PluginMetadata

    fun onLoad(context: PluginContext)

    fun onUnload()
}