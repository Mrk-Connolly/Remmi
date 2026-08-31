package com.remmi.app.core.database

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.plugin.model.models.RemmiModel

/**
 * DATABASE MANAGER
 *
 * Specialized manager for database-specific commands and service lifecycle.
 */
class DatabaseManager(
    private val eventBus: EventBus,
    val service: DatabaseService = SupabaseService
) : CommandListener {

    init {
        Log.d("Remmi", "[DatabaseManager] - Constructor initialized")
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is SaveDataCommand -> {
                Log.i("Remmi", "[DatabaseManager] - Global save requested by ${command.source}")
            }
            
            is UpsertDataCommand<*> -> {
                Log.i("Remmi", "[DatabaseManager] - Upserting item into ${command.tableName}")
                @Suppress("UNCHECKED_CAST")
                val typedCommand = command as UpsertDataCommand<RemmiModel>
                service.update(typedCommand.tableName, typedCommand.item, typedCommand.serializer)
            }
            
            is DeleteDataCommand -> {
                Log.i("Remmi", "[DatabaseManager] - Deleting item ${command.itemId} from ${command.tableName}")
                service.delete(command.tableName, command.itemId)
            }

            is FetchDataByIdCommand<*> -> {
                Log.i("Remmi", "[DatabaseManager] - Fetching item ${command.itemId} from ${command.tableName}")
                @Suppress("UNCHECKED_CAST")
                val typedCommand = command as FetchDataByIdCommand<RemmiModel>
                val result = service.getById(typedCommand.tableName, typedCommand.itemId, typedCommand.serializer)
                eventBus.publishEvent(
                    DataFetchedEvent(
                        items = listOfNotNull(result),
                        requestId = typedCommand.commandId,
                        correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                        causationId = typedCommand.commandId
                    )
                )
            }

            is FetchDataBySourceCommand<*> -> {
                Log.i("Remmi", "[DatabaseManager] - Fetching items for source ${command.sourcePlugin}/${command.sourceItemId} in ${command.tableName}")
                @Suppress("UNCHECKED_CAST")
                val typedCommand = command as FetchDataBySourceCommand<RemmiModel>
                val results = service.getBySource(typedCommand.tableName, typedCommand.sourcePlugin, typedCommand.sourceItemId, typedCommand.serializer)
                eventBus.publishEvent(
                    DataFetchedEvent(
                        items = results,
                        requestId = typedCommand.commandId,
                        correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                        causationId = typedCommand.commandId
                    )
                )
            }

            is FetchAllDataCommand<*> -> {
                Log.i("Remmi", "[DatabaseManager] - Fetching all items from ${command.tableName}")
                @Suppress("UNCHECKED_CAST")
                val typedCommand = command as FetchAllDataCommand<RemmiModel>
                val results = service.getAll(typedCommand.tableName, typedCommand.serializer)
                eventBus.publishEvent(
                    DataFetchedEvent(
                        items = results,
                        requestId = typedCommand.commandId,
                        correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                        causationId = typedCommand.commandId
                    )
                )
            }
        }
    }
}
