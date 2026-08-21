package com.remmi.app.plugins.recipebook

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.recipebook.ui.screens.RecipeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipePlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {

    private var _repository: RecipeRepository? = null
    private var _actions: RecipeActions? = null

    override val repository: RecipeRepository
        get() = _repository ?: throw IllegalStateException("RecipePlugin not initialized")

    override val actions: RecipeActions
        get() = _actions ?: throw IllegalStateException("RecipePlugin not initialized")

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@RecipePlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[RecipePlugin] - [Content] (widget) executed")
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[RecipePlugin] - [Content] (screen) executed")
            RecipeScreen(actions, controller)
        }
    }

    init {
        Log.d("Remmi", "[RecipePlugin] - Constructor initialized")
    }

    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[RecipePlugin] - Initializing")
        val repo = RecipeRepository(context.databaseManager.service)
        _repository = repo
        _actions = RecipeActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[RecipePlugin] - [onCommand] executed")
    }

    override suspend fun onEvent(event: RemmiEvent) {
        Log.d("Remmi", "[RecipePlugin] - [onEvent] executed")
    }

    override fun onLoad() {
        Log.d("Remmi", "[RecipePlugin] - [onLoad] executed")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync recipes", e)
            }
        }
    }

    override fun onUnload() {
        Log.d("Remmi", "[RecipePlugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        _repository?.clear()
    }
}
