package com.remmi.app.core.service.database

import android.util.Log
import com.remmi.app.core.events.commands.CommandListener
import com.remmi.app.core.events.commands.DeleteDataCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.commands.SaveDataCommand
import com.remmi.app.core.events.commands.UpsertDataCommand
import com.remmi.app.core.plugin.model.models.RemmiModel

/**
 * DATABASE MANAGER
 *
 * Specialized manager for database-specific commands and service lifecycle.
 */
class DatabaseManager : CommandListener {

    /** Database Service implementation */
    val service: DatabaseService = SupabaseService

    init {
        Log.d("Remmi", "[DatabaseManager] - Constructor initialized")
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is SaveDataCommand -> {
                Log.i("Remmi", "[DatabaseManager] - Global save requested by ${command.source}")
                // Implement global sync if needed
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
        }
    }
}
