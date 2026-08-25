package com.remmi.app.plugins.recipebook

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.plugins.recipebook.screens.RecipeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipePlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val eventBus: EventBus
) : RemmiPlugin {

    private val _repository = RecipeRepository(databaseManager.service)
    private val _actions = RecipeActions(_repository).apply {
        this.eventBus = this@RecipePlugin.eventBus
    }

    override val repository: RemmiRepository<out RemmiModel> get() = _repository
    override val actions: RecipeActions get() = _actions

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@RecipePlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[RecipePlugin] - [Content] (widget) executed")
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            RecipeScreen(actions, controller)
        }
    }

    override suspend fun initialize() {}

    override suspend fun onCommand(command: RemmiCommand) {}

    override suspend fun onEvent(event: RemmiEvent) {}

    override fun onLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        _repository.sync()
    }

    override fun onUnload() {}

    override suspend fun reformat() {
        _repository.clearAll()
    }
}
