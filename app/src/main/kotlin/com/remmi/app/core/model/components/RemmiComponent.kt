package com.remmi.app.core.model.components

import com.remmi.app.core.RemmiClass
import kotlin.time.Instant

/**
 * Interface for reusable data components that are parts of a [RemmiModel].
 *
 * Components (like [Location], [Reminder], [Priority]) are smaller pieces
 * of data that don't necessarily have a unique ID but follow the core schema.
 */
interface RemmiComponent : RemmiClass {
}
