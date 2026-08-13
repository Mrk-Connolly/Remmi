package com.remmi.app.core.plugins

import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.models.RemmiModel
import com.remmi.app.core.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.DatabaseService
import com.remmi.app.core.widgets.RemmiWidget

/**
 * Interface defining the structure and lifecycle of a Remmi Plugin.
 *
 * Each plugin must provide its own UI (screen and widget), data management (repository),
 * and business logic (actions).
 */
interface RemmiPlugin {

    /**
     * Metadata describing the plugin (id, name, version, etc.).
     */
    val metadata : PluginMetadata

    /**
     * The main UI screen for the plugin.
     */
    val screen : RemmiScreen

    /**
     * The dashboard widget for the plugin.
     */
    val widget : RemmiWidget

    /**
     * The action controller managing the plugin's logic.
     */
    val actions : RemmiAction

    /**
     * The repository managing the plugin's persistent data.
     */
    val repository : RemmiRepository<out RemmiModel>

    /**
     * Load plugin and items.
     */
    fun onLoad()

    /**
     * Called when the plugin is being unloaded (e.g., during app shutdown).
     */
    fun onUnload()

    /**
     * Reformat plugin database (clear all data).
     */
    suspend fun reformat()

}
