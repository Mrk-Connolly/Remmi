package com.remmi.app.core.model.models

import com.remmi.app.core.RemmiClass
import kotlin.time.Instant

/**
 * Base interface for all data models that can be managed by a [RemmiRepository].
 *
 * Every model must have a unique identifier and tracking for creation/modification times
 * to support synchronization and conflict resolution.
 */
interface RemmiModel : RemmiClass {

    /**
     * Unique identifier for the data item.
     */
    val id: String

    /**
     * Timestamp of when the item was first created.
     */
    val created: Instant

    /**
     * Timestamp of the last time the item was modified.
     */
    var modified: Instant
}
