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
import com.remmi.app.core.screens.HomeScreen
import androidx.compose.material3.Text
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.calendar.CalendarScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon

sealed class RemmiDestination {
    data object Home : RemmiDestination()
    data object Calendar : RemmiDestination()
    data object Tasks : RemmiDestination()
    data object Settings : RemmiDestination()
}

@Composable
fun AppNavigation(context: PluginContext) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home")
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Home,contentDescription = "Home" )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == "calendar",
                    onClick = { navController.navigate("calendar")
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.CalendarMonth,contentDescription = "Calendar" )
                    },
                    label = { Text("Calendar") }
                )

                NavigationBarItem(
                    selected = currentRoute == "tasks",
                    onClick = {navController.navigate("tasks")
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.CheckCircle,contentDescription = "Tasks" )
                    },
                    label = { Text("Tasks") }
                )

                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings")
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Settings,contentDescription = "Settings" )
                    },
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
                HomeScreen(context.widgetManager)
            }

            composable("calendar") {
                val calendarPlugin = context.pluginManager.plugins["calendar"] as? CalendarPlugin
                if (calendarPlugin != null) {
                    CalendarScreen(calendarPlugin.actions)
                } else {
                    Text("Calendar Plugin not loaded")
                }
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
