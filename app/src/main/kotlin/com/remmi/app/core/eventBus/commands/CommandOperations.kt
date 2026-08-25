package com.remmi.app.core.eventBus.commands

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * COMMAND OPERATIONS
 *
 * Handles the subscription and distribution of commands (requests for action).
 */
class CommandOperations {

    private val commandListeners = mutableSetOf<CommandListener>()

    private val _commands = MutableSharedFlow<RemmiCommand>(extraBufferCapacity = 64)
    val commands = _commands.asSharedFlow()

    /**
     * Register a new listener to receive action requests.
     */
    fun subscribe(listener: CommandListener) {
        Log.d("Remmi", "[CommandOperations] - Subscribing new CommandListener")
        commandListeners.add(listener)
    }

    /**
     * Remove a previously registered CommandListener.
     */
    fun unsubscribe(listener: CommandListener) {
        Log.d("Remmi", "[CommandOperations] - Unsubscribing CommandListener")
        commandListeners.remove(listener)
    }

    /**
     * Distribute an action request to all subscribed CommandListeners.
     */
    suspend fun publish(command: RemmiCommand) {
        Log.i("Remmi", "[CommandOperations] - COMMAND PUBLISHED: [${command::class.simpleName}] from [${command.source}] (ID: ${command.commandId})")
        
        _commands.emit(command)
        
        commandListeners.forEach { listener ->
            try {
                listener.onCommand(command)
            } catch (e: Exception) {
                Log.e("Remmi", "[CommandOperations] - CommandListener failure for [${command::class.simpleName}]: ${e.message}")
            }
        }
    }

    /**
     * Clear all command listeners.
     */
    fun clear() {
        commandListeners.clear()
    }
}
