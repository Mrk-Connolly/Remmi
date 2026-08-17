package com.remmi.app.core.events

/**
 * Interface representing a system-wide or plugin-specific event.
 *
 * Events are notifications of completed facts (facts).
 */
interface RemmiEvent {

    /** Unique identifier for the event instance */
    val eventId: String

    /** The system or plugin that originated the event (e.g., "calendar", "automation") */
    val source: String

    /** Standard type of the event */
    val type: EventType
}
