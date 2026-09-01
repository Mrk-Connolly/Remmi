package com.remmi.app.core.android.system

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * LOCATION SERVICE
 *
 * Interface for standard location operations.
 */
interface LocationService : CommandListener {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Request Current Location
     * Request the current device coordinates.
     * */
    fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit)
}
