package com.remmi.app.core.plugins.model.models

import kotlinx.datetime.Instant

/**
 * Base interface for all data models that can be managed by a [RemmiRepository].
 *
 * Every model must have a unique identifier and tracking for creation/modification times
 * to support synchronization and conflict resolution.
 */
interface RemmiModel {

    val id: String
    val created: Instant
    var modified: Instant
}
