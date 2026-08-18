package com.remmi.app.core.events

import java.util.UUID

/**
 * Generic event implementation for standard plugin CRUD operations.
 */
data class PluginEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val source: String,
    override val type: EventType,
    val itemId: String
) : RemmiEvent
