package com.remmi.app.core.events.events

import com.remmi.app.core.events.EventType
import com.remmi.app.core.events.RemmiMessage

/**
 * REMMI EVENT
 *
 * Interface representing a system-wide or plugin-specific notification.
 * Events are fact-based and describe something that has already happened.
 */
interface RemmiEvent : RemmiMessage {

    /** Unique identifier for the event instance */
    val eventId: String

    /** The system or plugin that originated the event (e.g., "calendar") */
    val source: String

    /** Standard type of the event */
    val type: EventType
}
