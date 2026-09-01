package com.remmi.app.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.remmi.app.core.host.RemmiHost
import com.remmi.app.ui.components.AppNavigation

/**
 * REMMI APPLICATION
 * 
 * Global entry point for the application.
 * Manages the singleton instance of RemmiHost to ensure consistency
 * between UI and background components (like Widgets).
 */
class RemmiApplication : Application() {

    lateinit var remmiHost: RemmiHost
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("Remmi", "[RemmiApplication] - Created")
        remmiHost = RemmiHost(this)
    }
}

/**
 * Remmi App UI root
 */
@Composable
fun RemmiApp(host: RemmiHost) {
    Log.d("Remmi", "[RemmiApp] - UI started")

    RemmiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // Start navigation, providing the runtime for plugin management
            AppNavigation(runtime = host.runtime)
        }
    }
}
