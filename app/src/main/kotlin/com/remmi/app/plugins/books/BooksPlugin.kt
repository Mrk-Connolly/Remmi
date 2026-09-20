package com.remmi.app.plugins.books

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class BooksPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {

    override val repository: RemmiRepository<RemmiModel> = object : RemmiRepository<RemmiModel> {
        override fun add(item: RemmiModel) {}
        override fun remove(id: String) {}
        override fun update(item: RemmiModel) {}
        override fun get(id: String): RemmiModel? = null
        override fun getAll(): List<RemmiModel> = emptyList()
        override fun asFlow(): Flow<List<RemmiModel>> = flowOf(emptyList())
        override fun clear() {}
    }

    override val actions: RemmiAction = object : RemmiAction {
        override val id: String = metadata.id
        override val name: String = metadata.name
        override var eventBus: EventBus? = this@BooksPlugin.eventBus
    }

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@BooksPlugin.metadata
        @Composable override fun Content() {
            Card {
                Text(text = " ${metadata.name} Plugin")
            }
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Text(text = "Welcome to ${metadata.name} Plugin")
        }
    }

    override suspend fun initialize() {}
    override suspend fun onCommand(command: RemmiCommand) {}
    override suspend fun onEvent(event: RemmiEvent) {}
    override fun onLoad() {}
    override suspend fun refresh() {}
    override fun onUnload() {}
    override suspend fun reformat() {}
}
