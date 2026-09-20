package com.remmi.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope

import com.remmi.app.core.host.RemmiHost
import com.remmi.app.ui.RemmiApp
import com.remmi.app.ui.RemmiApplication
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

        // Access the shared Remmi core system from Application
        remmiHost = (application as RemmiApplication).remmiHost
        
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
        // We no longer stop the host here because it's application-scoped
        // and might be needed by the widget.
    }
}
