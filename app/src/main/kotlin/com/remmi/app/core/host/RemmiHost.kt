package com.remmi.app.core.host

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.navigation.AppNavigation
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.runtime.RemmiRuntime

/**
 * Remmi Host
 * Executes Runtime and UI menu
 */

@Composable
fun RemmiHost() {
    Log.d("Remmi", "[RemmiHost] - [RemmiHost] executed")
    Log.d("Remmi", "Starting host service")

    val hostContext = HostContext(
        automationEngine = AutomationEngine(),
        pluginManager = PluginManager(),
        androidContext = LocalContext.current
    )

    // ------------------ 1º Executes startup script ------------------


    // Create this object once, and keep it between recompositions
    val runtime = remember {

        // Construct class and Executes first
        RemmiRuntime(hostContext).apply {
            start()
        }
    }

    Log.d("Remmi", "Starting navigation panel")
    // 3. Start ui bottom menu navigation
    AppNavigation(context = hostContext)
}
