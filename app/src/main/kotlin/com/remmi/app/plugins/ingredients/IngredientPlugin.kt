package com.remmi.app.plugins.ingredients

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.ingredients.models.*
import com.remmi.app.plugins.ingredients.ui.screens.IngredientStockScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Ingredient plugin via EventBus.
 */
class IngredientPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _metadataRepo: MetadataRepository = MetadataRepository()
    private val _stockRepo: StockRepository = StockRepository()
    private val _batchRepo: BatchRepository = BatchRepository()
    
    private val _actions: IngredientActions = IngredientActions(_metadataRepo, _stockRepo, _batchRepo).apply {
        this.eventBus = this@IngredientPlugin.eventBus
    }

    /** Primary repository (metadata) */
    override val repository: MetadataRepository get() = _metadataRepo

    /** Action controller for ingredient logic. */
    override val actions: IngredientActions get() = _actions

    /** Dashboard widget for ingredients. */
    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@IngredientPlugin.metadata
        @Composable override fun Content() {}
    }

    /** UI screen for ingredient management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[IngredientPlugin] - [Content] executed")
            IngredientStockScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[IngredientPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[IngredientPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Ingredient plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[IngredientPlugin] - Received command: ${command::class.simpleName}")
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is DataFetchedEvent<*> -> {
                if (event.items.isNotEmpty()) {
                    val first = event.items[0]
                    when (first) {
                        is IngredientMetadata -> {
                            _metadataRepo.clear()
                            @Suppress("UNCHECKED_CAST")
                            (event.items as List<IngredientMetadata>).forEach { _metadataRepo.add(it) }
                            Log.d("Remmi", "[IngredientPlugin] - Updated metadata repository")
                        }
                        is UserStock -> {
                            _stockRepo.clear()
                            @Suppress("UNCHECKED_CAST")
                            (event.items as List<UserStock>).forEach { _stockRepo.add(it) }
                            Log.d("Remmi", "[IngredientPlugin] - Updated stock repository")
                        }
                        is StockBatch -> {
                            _batchRepo.clear()
                            @Suppress("UNCHECKED_CAST")
                            (event.items as List<StockBatch>).forEach { _batchRepo.add(it) }
                            Log.d("Remmi", "[IngredientPlugin] - Updated batch repository")
                        }
                    }
                }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[IngredientPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Ingredients Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Ingredients Plugin Loaded")
    }

    /**                                   Refresh
     * Sync ingredients with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[IngredientPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync ingredients: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[IngredientPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[IngredientPlugin] - [reformat] executed")
        _metadataRepo.clear()
        _stockRepo.clear()
        _batchRepo.clear()
    }
}
