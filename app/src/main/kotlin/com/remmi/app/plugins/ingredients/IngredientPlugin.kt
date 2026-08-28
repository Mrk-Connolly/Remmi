package com.remmi.app.plugins.ingredients

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.FetchIngredientMetadataCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.eventBus.events.IngredientMetadataFetchedEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.plugins.ingredients.repository.MetadataRepository
import com.remmi.app.plugins.ingredients.repository.StockRepository
import com.remmi.app.plugins.ingredients.repository.BatchRepository
import com.remmi.app.plugins.ingredients.screens.IngredientStockScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val eventBus: EventBus
) : RemmiPlugin {

    private val _repositoryMetadata = MetadataRepository(databaseManager.service)
    private val _repositoryStock = StockRepository(databaseManager.service)
    private val _repositoryBatch = BatchRepository(databaseManager.service)
    
    private val _actions = IngredientActions(
        _repositoryMetadata,
        _repositoryStock,
        _repositoryBatch
    ).apply {
        this.eventBus = this@IngredientPlugin.eventBus
    }

    override val repository: RemmiRepository<out RemmiModel> get() = _repositoryMetadata
    val repositoryStock get() = _repositoryStock
    val repositoryBatch get() = _repositoryBatch

    override val actions: IngredientActions get() = _actions

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@IngredientPlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[IngredientPlugin] - [Content] (widget) executed")
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            IngredientStockScreen(actions, controller)
        }
    }

    override suspend fun initialize() {
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is FetchIngredientMetadataCommand -> {
                val data = actions.getMetadataList()
                eventBus.publishEvent(IngredientMetadataFetchedEvent(data))
            }
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is com.remmi.app.core.eventBus.events.ReceiptImageSelectedEvent -> {
                Log.i("Remmi", "[IngredientPlugin] - Receipt image selected, requesting OCR")
                eventBus.publishCommand(
                    com.remmi.app.core.eventBus.commands.RequestOCRCommand(
                        imageUri = event.imageUri,
                        requestId = event.requestId,
                        correlationId = event.correlationId
                    )
                )
            }
        }
    }

    override fun onLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        _repositoryMetadata.sync()
        _repositoryStock.sync()
        _repositoryBatch.sync()
    }

    override fun onUnload() {}

    override suspend fun reformat() {
        _repositoryMetadata.clearAll()
        _repositoryStock.clearAll()
        _repositoryBatch.clearAll()
    }
}
