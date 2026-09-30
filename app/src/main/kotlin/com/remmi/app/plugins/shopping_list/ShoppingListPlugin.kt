package com.remmi.app.plugins.shopping_list

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.CreateShoppingItemCommand
import com.remmi.app.core.eventBus.commands.DeleteShoppingItemCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.ToggleShoppingItemCommand
import com.remmi.app.core.plugin.BaseRemmiPlugin
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.shopping_list.models.ShoppingItem

class ShoppingListPlugin(
    metadata: PluginMetadata,
    eventBus: EventBus
) : BaseRemmiPlugin<ShoppingItem>(metadata, eventBus, ShoppingItem::class.java) {

    private val _repository = ShoppingRepository()
    private val _actions = ShoppingActions(_repository).apply {
        this.eventBus = this@ShoppingListPlugin.eventBus
    }

    override val repository: ShoppingRepository get() = _repository
    override val actions: ShoppingActions get() = _actions

    override val widget: RemmiWidget by lazy {
        object : RemmiWidget {
            override val metadata: PluginMetadata = this@ShoppingListPlugin.metadata
            @Composable
            override fun Content() {
                androidx.compose.material3.Text("Shopping List Plugin")
            }
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content(controller: RemmiController) {
            androidx.compose.material3.Text("Welcome to Shopping List")
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        super.onCommand(command)
        Log.d("Remmi", "[ShoppingListPlugin] - Command received: ${command::class.simpleName}")
        when (command) {
            is CreateShoppingItemCommand -> {
                _actions.addItem(command.name, command.quantity)
            }
            is ToggleShoppingItemCommand -> {
                _actions.toggleItem(command.itemId)
            }
            is DeleteShoppingItemCommand -> {
                _actions.deleteItem(command.itemId)
            }
        }
    }
}
