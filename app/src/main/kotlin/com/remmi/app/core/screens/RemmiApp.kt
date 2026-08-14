package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.remmi.app.core.host.RemmiHost
import com.remmi.app.core.navigation.AppNavigation

/**
 * Remmi App UI root
 */
@Composable
fun RemmiApp(host: RemmiHost) {
    Log.d("Remmi", "[RemmiApp] - UI started")

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // Start navigation, providing the runtime for plugin management
            AppNavigation(runtime = host.runtime)
        }
    }
}
