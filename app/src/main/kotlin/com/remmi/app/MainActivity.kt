package com.remmi.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope

import com.remmi.app.core.host.RemmiHost
import com.remmi.app.core.screens.RemmiApp
import com.remmi.app.core.util.ErrorToaster
import kotlinx.coroutines.launch

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

        super.onCreate(savedInstanceState)

        // Allow any layer to surface user-visible errors (e.g. DB failures)
        ErrorToaster.init(applicationContext)

        // Initialize the Remmi core system
        remmiHost = RemmiHost(applicationContext)
        
        // Start the system in a coroutine
        lifecycleScope.launch {
            remmiHost.start()
        }

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
