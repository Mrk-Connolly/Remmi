package com.remmi.app.core.plugin.actions

import com.remmi.app.core.events.EventBus

/**
 * Base interface for all actions within the Remmi ecosystem.
 *
 * REMMI ACTIONS are large customized to each plugin, they do not follow a firm
 * structure.
 */
interface RemmiAction {

    // ----------------------------------------------------------------------------
    //                             INTERFACE VARIABLES
    // ----------------------------------------------------------------------------

    /** Unique identifier for the action set */
    val id: String

    /** Human-readable name for the actions */
    val name: String

    /** Shared system event bus for publishing completed facts */
    var eventBus: EventBus?


    // ----------------------------------------------------------------------------
    //                               CORE OPERATIONS
    // ----------------------------------------------------------------------------

}
