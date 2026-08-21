package com.remmi.app.core.database

import android.util.Log
import com.remmi.app.core.events.CommandListener
import com.remmi.app.core.events.DeleteDataCommand
import com.remmi.app.core.events.RemmiCommand
import com.remmi.app.core.events.SaveDataCommand
import com.remmi.app.core.events.UpsertDataCommand
import com.remmi.app.core.plugins.model.models.RemmiModel

/**
 * DATABASE SERVICE MANAGER
 *
 * Specialized manager for database-specific commands and service lifecycle.
 */
class DatabaseServiceManager : CommandListener {

    /** Database Service implementation */
    val service: DatabaseService = SupabaseService

    init {
        Log.d("Remmi", "[DatabaseServiceManager] - Constructor initialized")
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is SaveDataCommand -> {
                Log.i("Remmi", "[DatabaseServiceManager] - Global save requested by ${command.source}")
                // Implement global sync if needed
            }
            
            is UpsertDataCommand<*> -> {
                Log.i("Remmi", "[DatabaseServiceManager] - Upserting item into ${command.tableName}")
                @Suppress("UNCHECKED_CAST")
                val typedCommand = command as UpsertDataCommand<RemmiModel>
                service.update(typedCommand.tableName, typedCommand.item, typedCommand.serializer)
            }
            
            is DeleteDataCommand -> {
                Log.i("Remmi", "[DatabaseServiceManager] - Deleting item ${command.itemId} from ${command.tableName}")
                service.delete(command.tableName, command.itemId)
            }
        }
    }
}
