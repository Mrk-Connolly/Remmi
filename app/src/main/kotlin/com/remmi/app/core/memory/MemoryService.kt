package com.remmi.app.core.memory

import android.util.Log
import com.remmi.app.core.android.services.SystemSettingsService
import com.remmi.app.core.controller.RemmiComponent
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.plugin.model.models.RemmiModel

/**
 * MEMORY SERVICE
 *
 * Central coordinator for Remmi storage.
 * Listens to EventBus commands and delegates to the active MemoryProvider.
 */
class MemoryService(
    private val eventBus: EventBus,
    private val settingsService: SystemSettingsService,
    private val providers: Map<MemoryProviderType, MemoryProvider>
) : RemmiComponent, CommandListener {

    companion object {
        private const val TAG = "MemoryService"
        private const val SETTING_PROVIDER_TYPE = "memory_provider_type"
    }

    /** The currently active provider resolved from settings */
    private val activeProvider: MemoryProvider
        get() {
            val typeStr = settingsService.getString(SETTING_PROVIDER_TYPE, MemoryProviderType.DATABASE.name)
            val type = try {
                MemoryProviderType.valueOf(typeStr ?: MemoryProviderType.DATABASE.name)
            } catch (e: Exception) {
                MemoryProviderType.DATABASE
            }
            return providers[type] ?: providers[MemoryProviderType.DATABASE]!!
        }

    override suspend fun start() {
        Log.d(TAG, "[MemoryService] - Starting and subscribing to EventBus")
        eventBus.subscribeCommand(this)
    }

    override fun stop() {
        Log.d(TAG, "[MemoryService] - Stopping and unsubscribing from EventBus")
        eventBus.unsubscribeCommand(this)
    }

    override suspend fun onCommand(command: RemmiCommand) {
        try {
            when (command) {
                is UpsertDataCommand<*> -> {
                    Log.i(TAG, "Delegating Upsert to active provider")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as UpsertDataCommand<RemmiModel>
                    activeProvider.upsert(typedCommand.tableName, typedCommand.item, typedCommand.serializer)
                }

                is DeleteDataCommand -> {
                    Log.i(TAG, "Delegating Delete to active provider")
                    activeProvider.delete(command.tableName, command.itemId)
                }

                is FetchDataByIdCommand<*> -> {
                    Log.i(TAG, "Delegating FetchById to active provider")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchDataByIdCommand<RemmiModel>
                    val result = activeProvider.getById(typedCommand.tableName, typedCommand.itemId, typedCommand.serializer)
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
                    Log.i(TAG, "Delegating FetchBySource to active provider")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchDataBySourceCommand<RemmiModel>
                    val results = activeProvider.getBySource(
                        typedCommand.tableName,
                        typedCommand.sourcePlugin,
                        typedCommand.sourceItemId,
                        typedCommand.serializer
                    )
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
                    Log.i(TAG, "Delegating FetchAll to active provider")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchAllDataCommand<RemmiModel>
                    val results = activeProvider.getAll(typedCommand.tableName, typedCommand.serializer)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error handling command ${command::class.simpleName}: ${e.message}", e)
        }
    }
}
