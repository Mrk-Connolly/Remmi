package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.host.RemmiHost
import com.remmi.app.core.navigation.AppNavigation

/**
 * Remmi App UI root
 */
@Composable
fun RemmiApp(host: RemmiHost) {
    Log.d("Remmi", "[RemmiApp] - UI started")

    // Start navigation, providing the runtime for plugin management
    AppNavigation(runtime = host.runtime)
}
