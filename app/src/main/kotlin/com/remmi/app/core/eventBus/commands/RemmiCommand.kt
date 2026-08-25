package com.remmi.app.core.eventBus.commands

import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.RemmiMessage

/**
 * REMMI COMMAND
 *
 * Interface representing a request for an action to be performed by another system.
 * Commands are intent-based and describe what should happen (e.g., "Delete Alarm").
 */
interface RemmiCommand : RemmiMessage {

    /** Unique identifier for the command instance */
    val commandId: String

    /** The system or engine that issued the command (e.g., "automation") */
    val source: String

    /** Context for creation commands */
    val creationContext: CreationContext?

    /** Context for deletion commands */
    val deletionContext: DeletionContext?
}
