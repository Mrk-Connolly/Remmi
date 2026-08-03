package com.remmi.app.core.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.remmi.app.HomeScreen
import androidx.compose.material3.Text
import com.remmi.app.core.widgets.WidgetManager

sealed class RemmiDestination {
    data object Home : RemmiDestination()
    data object Calendar : RemmiDestination()
    data object Tasks : RemmiDestination()
    data object Settings : RemmiDestination()
}

@Composable
fun AppNavigation(widgetManager: WidgetManager) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Text("🏠") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Text("📅") },
                    label = { Text("Calendar") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Text("✓") },
                    label = { Text("Tasks") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Text("⚙") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(widgetManager)
            }

            composable("calendar") {
                Text("Calendar")
            }

            composable("tasks") {
                Text("Tasks")
            }

            composable("settings") {
                Text("Settings")
            }
        }
    }
}