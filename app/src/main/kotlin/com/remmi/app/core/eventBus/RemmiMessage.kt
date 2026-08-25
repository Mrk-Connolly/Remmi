package com.remmi.app.core.eventBus

/**
 * REMMI MESSAGE
 * Base interface for all communication messages in the Remmi system.
 */
interface RemmiMessage {
    /** ID of the initial message that started the chain */
    val correlationId: String?

    /** ID of the message that directly caused this message */
    val causationId: String?
}
