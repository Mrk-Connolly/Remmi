package com.remmi.app.core.host

import android.content.Context
import android.util.Log
import com.remmi.app.core.controller.RemmiController

/**
 * Remmi Host
 * Owns the app environment and builds the system
 */
class RemmiHost(val androidContext: Context) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    val runtime = RemmiController(androidContext)




    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * */
    init {
        Log.d("Remmi", "[RemmiHost] - Constructor initialized")
    }



    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Calls runtime start function
     * */
    fun start() {
        Log.d("Remmi", "[RemmiHost] - Starting system")
        // Start core functions and load plugins
        runtime.start()
    }

    /**                                 Stop
     * Calls runtime stop function
     * */
    fun stop() {
        Log.d("Remmi", "[RemmiHost] - Stopping system")
        runtime.stop()
    }
}
