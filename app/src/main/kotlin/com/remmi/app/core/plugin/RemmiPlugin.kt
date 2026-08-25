package com.remmi.app.core.plugin

import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.EventListener
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.model.models.PluginAction
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget

/**
 * Interface defining the structure and lifecycle of a Remmi Plugin.
 *
 * Each plugin must provide its own UI (screen and widget), data management (repository),
 * and business logic (actions).
 */
interface RemmiPlugin : CommandListener, EventListener {


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

    /** Generic actions exposed by this plugin to other plugins. */
    val exposedActions: List<PluginAction> get() = emptyList()


    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     * Must be called before any other operation.
     */
    suspend fun initialize()

    /**                                   On Command
     * Handle a command specifically targeted at this plugin.
     * */
    override suspend fun onCommand(command: RemmiCommand)

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent)

    /**                                   Load
     * Load plugin items and prepare for execution.
     */
    fun onLoad()

    /**                                   Refresh
     * Refresh plugin data from its source.
     */
    suspend fun refresh()

    /**                                   Unload
     * Called when the plugin is being unloaded (e.g., during app shutdown).
     */
    fun onUnload()

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    suspend fun reformat()

}
