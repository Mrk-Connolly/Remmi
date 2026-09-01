package com.remmi.app.plugins.recipebook

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.eventBus.events.IngredientMetadataFetchedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.recipebook.models.RecipeItem
import com.remmi.app.plugins.recipebook.ui.screens.RecipeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Recipe Book plugin via EventBus.
 */
class RecipePlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: RecipeRepository = RecipeRepository()
    private val _actions: RecipeActions = RecipeActions(_repository).apply {
        this.eventBus = this@RecipePlugin.eventBus
    }

    /** Repository for managing Recipe data */
    override val repository: RecipeRepository get() = _repository

    /** Action controller for recipe logic. */
    override val actions: RecipeActions get() = _actions

    /** Dashboard widget for recipe. */
    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@RecipePlugin.metadata
        @Composable override fun Content() {}
    }

    /** UI screen for recipe management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[RecipePlugin] - [Content] executed")
            RecipeScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[RecipePlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[RecipePlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Recipe plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[RecipePlugin] - Received command: ${command::class.simpleName}")
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is DataFetchedEvent<*> -> {
                if (event.items.isNotEmpty() && event.items[0] is RecipeItem) {
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<RecipeItem>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[RecipePlugin] - Updated repository with ${event.items.size} recipes")
                }
            }
            is IngredientMetadataFetchedEvent -> {
                Log.i("Remmi", "[RecipePlugin] - Metadata received. Triggering nutrition recalculation.")
                actions.performRecalculation(event.metadata)
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[RecipePlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Recipe Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Recipe Plugin Loaded")
    }

    /**                                   Refresh
     * Sync recipes with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[RecipePlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync recipes: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[RecipePlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[RecipePlugin] - [reformat] executed")
        _repository.clear()
    }
}
