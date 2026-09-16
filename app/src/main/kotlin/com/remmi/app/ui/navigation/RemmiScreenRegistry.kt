package com.remmi.app.ui.navigation

import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController

/**
 * Global registry for plugin-provided screens.
 * Decouples the central NavHost from individual plugin routes.
 */
object RemmiScreenRegistry {
    private val screens = mutableMapOf<String, @Composable (RemmiController) -> Unit>()

    fun register(pluginId: String, content: @Composable (RemmiController) -> Unit) {
        screens[pluginId] = content
    }

    fun getScreen(pluginId: String): (@Composable (RemmiController) -> Unit)? {
        return screens[pluginId]
    }

    fun getAllRegisteredPluginIds(): Set<String> {
        return screens.keys
    }
}
