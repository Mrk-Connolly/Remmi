package com.remmi.app.core.eventBus

/**
 * Context in which a data item was created.
 */
enum class CreationContext {
    PRIMARY,           // Main item created by user
    SECONDARY_LINKED,  // Created automatically due to a relationship
    AUTOMATION,        // Created by automation engine
    MANUAL             // Explicitly created by user in a standalone way
}

/**
 * Context in which a data item was deleted.
 */
enum class DeletionContext {
    PRIMARY,           // The item itself was deleted by user intent
    LINKED_CLEANUP,    // Deleted because its source was deleted
    AUTOMATION,        // Deleted by automation engine
    MANUAL             // Explicitly deleted by user
}
