package com.remmi.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.remmi.app.core.host.RemmiHost
import com.remmi.app.core.screens.RemmiApp

class MainActivity : ComponentActivity() {
    // Android launches this class when the user opens Remmi.

    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------

    /** RemmiHost is the lead function that executes core functions and their environment*/
    private lateinit var remmiHost: RemmiHost






    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                               On Create
     * On create start activity by calling Remmi Application ui and Remmi Hosting
     * manager by overriding parent class
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Remmi", "[MainActivity] - App started")

        /**
         * Bundle : Saves remmiHost in a bundle so that android actions
         * can erase their information nor stop their function
         *
         * Calls the onCreate from parent class, dont know why
         * */

        super.onCreate(savedInstanceState)

        // Initialize the Remmi core system
        remmiHost = RemmiHost(applicationContext)
        remmiHost.start()

        // Initialise and run UI system
        enableEdgeToEdge()
        setContent {
            RemmiApp(remmiHost)
        }
    }

    /**                               On Destroy
     * On destroy end activity from Remmi Application overriding parent class
     * */
    override fun onDestroy() {
        super.onDestroy()
        remmiHost.stop()
    }
}
