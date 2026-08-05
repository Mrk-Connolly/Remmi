package com.remmi.app.core.host

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.remmi.app.core.navigation.AppNavigation
import com.remmi.app.core.runtime.RemmiRuntime

@Composable
fun RemmiHost() { // Hosts the actual application

    Log.d("Remmi", "Runtime generated")

    // Android environment
    // Used to load json files
    val androidContext = LocalContext.current

    // 1. Create the Runtime
    // Runtime executes startup script
    val runtime = remember { // Create this object once, and keep it between recompositions

        RemmiRuntime(androidContext).apply { // Construct class and Executes first

            Log.d("Remmi", "Runtime executed")

            // 2. Start loading services and plugins.
            start()
        }
    }


    // 3. Start ui bottom menu navigation
    AppNavigation(widgetManager = runtime.controller.widgetManager)
}
