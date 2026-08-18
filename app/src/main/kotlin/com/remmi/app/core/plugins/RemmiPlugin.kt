package com.remmi.app.core.plugins

import com.remmi.app.core.events.RemmiCommand
import com.remmi.app.core.events.RemmiEvent
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.core.plugins.model.models.RemmiModel
import com.remmi.app.core.plugins.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugins.widgets.RemmiWidget

/**
 * Interface defining the structure and lifecycle of a Remmi Plugin.
 *
 * Each plugin must provide its own UI (screen and widget), data management (repository),
 * and business logic (actions).
 */
interface RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                             INTERFACE VARIABLES
    // ----------------------------------------------------------------------------

    /** Metadata describing the plugin (id, name, version, etc.). */
    val metadata : PluginMetadata

    /** The main UI screen for the plugin. */
    val screen : RemmiScreen

    /** The dashboard widget for the plugin. */
    val widget : RemmiWidget

    /** The action controller managing the plugin's logic. */
    val actions : RemmiAction

    /** The repository managing the plugin's persistent data. */
    val repository : RemmiRepository<out RemmiModel>


    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     * Must be called before any other operation.
     */
    suspend fun initialize(context: PluginContext)

    /**                                   On Command
     * Handle a command specifically targeted at this plugin.
     * */
    suspend fun onCommand(command: RemmiCommand)

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    suspend fun onEvent(event: RemmiEvent)

    /**                                   Load
     * Load plugin items and prepare for execution.
     */
    fun onLoad()

    /**                                   Unload
     * Called when the plugin is being unloaded (e.g., during app shutdown).
     */
    fun onUnload()

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    suspend fun reformat()

}
