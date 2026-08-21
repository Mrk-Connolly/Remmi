package com.remmi.app.plugins.ingredients

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.RemmiCommand
import com.remmi.app.core.events.RemmiEvent
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.ingredients.repository.MetadataRepository
import com.remmi.app.plugins.ingredients.repository.StockRepository
import com.remmi.app.plugins.ingredients.repository.BatchRepository
import com.remmi.app.plugins.ingredients.ui.screens.IngredientStockScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {

    private var _actions: IngredientActions? = null
    private var _metadataRepo: MetadataRepository? = null
    private var _stockRepo: StockRepository? = null
    private var _batchRepo: BatchRepository? = null

    override val repository: MetadataRepository
        get() = _metadataRepo ?: throw IllegalStateException("IngredientPlugin not initialized")

    val repositoryStock: StockRepository
        get() = _stockRepo ?: throw IllegalStateException("IngredientPlugin not initialized")

    val repositoryBatch: BatchRepository
        get() = _batchRepo ?: throw IllegalStateException("IngredientPlugin not initialized")

    override val actions: IngredientActions
        get() = _actions ?: throw IllegalStateException("IngredientPlugin not initialized")

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@IngredientPlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[IngredientPlugin] - Widget Content")
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            IngredientStockScreen(actions, controller)
        }
    }

    override suspend fun initialize(context: PluginContext) {
        val db = context.databaseManager.service
        val mRepo = MetadataRepository(db)
        val sRepo = StockRepository(db)
        val bRepo = BatchRepository(db)
        
        _metadataRepo = mRepo
        _stockRepo = sRepo
        _batchRepo = bRepo
        _actions = IngredientActions(mRepo, sRepo, bRepo).apply {
            this.eventBus = context.eventBus
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        // Implement ingredient specific commands if needed
    }

    override suspend fun onEvent(event: RemmiEvent) {
        // Handle events like IngredientExpired etc.
    }

    override fun onLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
    }

    override fun onUnload() {}

    override suspend fun reformat() {
        // Clear all repos
    }
}
