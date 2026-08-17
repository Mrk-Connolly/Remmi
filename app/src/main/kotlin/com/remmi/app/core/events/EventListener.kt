package com.remmi.app.core.events

/**
 * Interface for components that listen for system events
 */
interface EventListener {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Event
     * Callback executed when a RemmiEvent is published to the system
     * */
    fun onEvent(event: RemmiEvent)

}
