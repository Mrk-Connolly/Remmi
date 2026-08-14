package com.remmi.app.core.plugins.actions

/**
 * Base interface for all actions within the Remmi ecosystem.
 */
interface RemmiAction {

    /**
     * REMMI ACTIONS  are large customized to each plugin, they do not follow a firm
     * structure.
     *
     * One may be created when implementing the Automation Engine
     *
     * The Interface in either case will still exist as a structural waypoint
     * */

    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------


    val id: String
    val name: String


    // ----------------------------------------------------------------------------
    //                                 CORE OPERATIONS
    // ----------------------------------------------------------------------------

}
