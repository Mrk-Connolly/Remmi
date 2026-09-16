package com.remmi.app.core.plugin

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BASE REMMI PLUGIN
 *
 * Abstract base class that provides standard lifecycle and synchronization logic
 * for all Remmi plugins. Reduces boilerplate for data fetching and EventBus management.
 */
abstract class BaseRemmiPlugin<T : RemmiModel>(
    override val metadata: PluginMetadata,
    protected val eventBus: EventBus,
    private val modelClass: Class<T>
) : RemmiPlugin {

    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    override suspend fun initialize() {
        Log.d("Remmi", "[${metadata.id}Plugin] - Initializing")
        // Register screen in the global registry
        com.remmi.app.ui.navigation.RemmiScreenRegistry.register(metadata.id) { controller ->
            screen.Content(controller = controller)
        }
    }

    override fun onLoad() {
        Log.d("Remmi", "[${metadata.id}Plugin] - [onLoad] executed")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        Log.d("Remmi", "[${metadata.id}Plugin] - Refreshing data")
        (actions as? com.remmi.app.core.plugin.actions.BaseRemmiAction)?.sync()
    }

    override fun onUnload() {
        Log.d("Remmi", "[${metadata.id}Plugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        Log.d("Remmi", "[${metadata.id}Plugin] - [reformat] executed")
        repository.clear()
    }

    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    override suspend fun onCommand(command: RemmiCommand) {
        // Base handles generic commands, sub-plugins override for specifics
    }

    override suspend fun onEvent(event: RemmiEvent) {
        if (event is DataFetchedEvent<*>) {
            handleDataFetched(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected open suspend fun handleDataFetched(event: DataFetchedEvent<*>) {
        if (event.items.isNotEmpty()) {
            val firstItem = event.items[0]
            if (modelClass.isInstance(firstItem)) {
                // By default, if the items match the primary model, update repository
                (repository as? com.remmi.app.core.plugin.repository.MemoryRepository<T>)?.let { repo ->
                    repo.clear()
                    (event.items as List<T>).forEach { repo.add(it) }
                    Log.d("Remmi", "[${metadata.id}Plugin] - Synced ${event.items.size} items to repository")
                }
            }
        }
    }
}
