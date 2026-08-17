package com.remmi.app.core.plugins.actions

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


    // ----------------------------------------------------------------------------
    //                               CORE OPERATIONS
    // ----------------------------------------------------------------------------

}
