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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon

sealed class RemmiDestination(val route: String) {
    data object Home : RemmiDestination("home")
    data object Calendar : RemmiDestination("calendar")
    data object Tasks : RemmiDestination("tasks")
    data object Alarm : RemmiDestination("alarm")
    data object Settings : RemmiDestination("settings")
}

/**
 * REMMI DESTINATION  is the bottom menu to access all plugins
 *
 * only contains 1 function
 * */

// ----------------------------------------------------------------------------
//                                 APP NAVIGATION
// ----------------------------------------------------------------------------

@Composable
fun AppNavigation(context: PluginContext) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntry?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.Home.route,
                    onClick = { navController.navigate(RemmiDestination.Home.route)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Home,contentDescription = "Home" )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.Calendar.route,
                    onClick = { navController.navigate(RemmiDestination.Calendar.route)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.CalendarMonth,contentDescription = "Calendar" )
                    },
                    label = { Text("Calendar") }
                )

                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.Tasks.route,
                    onClick = {navController.navigate(RemmiDestination.Tasks.route)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.CheckCircle,contentDescription = "Tasks" )
                    },
                    label = { Text("Tasks") }
                )

                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.Alarm.route,
                    onClick = { navController.navigate(RemmiDestination.Alarm.route)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = "Alarm")
                    },
                    label = { Text("Alarm") }
                )

                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.Settings.route,
                    onClick = { navController.navigate(RemmiDestination.Settings.route)
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
            startDestination = RemmiDestination.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(RemmiDestination.Home.route) {
                HomeScreen(context.widgetManager)
            }

            composable(RemmiDestination.Calendar.route) {
                context.pluginManager.plugins["calendar"]?.screen?.Content() ?: Text("Calendar Plugin not loaded")
            }

            composable(RemmiDestination.Tasks.route) {
                context.pluginManager.plugins["tasks"]?.screen?.Content() ?: Text("Tasks Plugin not loaded")
            }

            composable(RemmiDestination.Alarm.route) {
                context.pluginManager.plugins["alarm"]?.screen?.Content() ?: Text("Alarm Plugin not loaded")
            }

            composable(RemmiDestination.Settings.route) {
                Text("Settings")
            }
        }
    }
}
