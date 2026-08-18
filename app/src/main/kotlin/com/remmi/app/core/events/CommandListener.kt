package com.remmi.app.core.events

/**
 * COMMAND LISTENER
 *
 * Interface for components that can execute system commands.
 */
fun interface CommandListener {

    /**                                 On Command
     * Callback executed when a RemmiCommand is published to the system.
     * Implementations should inspect the command and execute the relevant action.
     * */
    suspend fun onCommand(command: RemmiCommand)
}
