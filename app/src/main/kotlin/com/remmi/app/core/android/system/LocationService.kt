package com.remmi.app.core.android.system

/**
 * LOCATION SERVICE
 *
 * Interface for standard location operations.
 */
interface LocationService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Request Current Location
     * Request the current device coordinates.
     * */
    fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit)
}
