package com.remmi.app.core.plugin.model.models

import kotlinx.datetime.Instant

/**
 * Base interface for all data models that can be managed by a [RemmiRepository].
 *
 * Every model must have a unique identifier and tracking for creation/modification times
 * to support synchronization and conflict resolution.
 */
interface RemmiModel {

    // ----------------------------------------------------------------------------
    //                             INTERFACE VARIABLES
    // ----------------------------------------------------------------------------

    /** Unique identifier for the model instance */
    val id: String

    /** Creation timestamp */
    val created: Instant

    /** Last modification timestamp */
    var modified: Instant

    /** Owner of the data item */
    val userId: String?

    /** The plugin that caused this item to be created (optional) */
    val sourcePlugin: String?

    /** The ID of the item in the source plugin that caused this item to be created (optional) */
    val sourceItemId: String?
}
