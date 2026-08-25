package com.remmi.app.core.eventBus

import com.remmi.app.core.eventBus.events.RemmiEvent
import java.util.UUID

/**
 * Generic event implementation for standard plugin CRUD operations.
 */
data class PluginEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val source: String,
    override val type: EventType,
    val itemId: String,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null,
    override val correlationId: String? = null,
    override val causationId: String? = null
) : RemmiEvent
