package com.remmi.app.core.events

import com.remmi.app.core.RemmiClass

/**
 * Interface representing a system-wide or plugin-specific event.
 *
 * Events are used to notify different parts of the system about changes
 * or triggers (e.g., "Calendar synced", "Task completed").
 */
interface RemmiEvent : RemmiClass {
}
