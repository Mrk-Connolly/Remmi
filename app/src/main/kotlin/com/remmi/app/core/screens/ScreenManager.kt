package com.remmi.app.core.screens

import com.remmi.app.core.plugins.PluginRegistry

class ScreenManager(private val registry: PluginRegistry) {

    fun getScreens(): List<RemmiScreen> {

        return registry
            .getPlugins()
            .mapNotNull {
                it.getScreen()
            }

    }
}