package com.remmi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.screens.homescreen.HomeScreen
import com.remmi.app.ui.screens.settings.AutomatizationSettingsScreen
import com.remmi.app.ui.screens.settings.SettingsScreen
import com.remmi.app.ui.components.getIconForName

/**
 * Main navigation orchestrator for the Remmi application.
 */
@Composable
fun AppNavigation(
    runtime: RemmiController
) {
    val navController = rememberNavController()
    val isEditorActive by GlobalUIState.isEditorActive

    Scaffold(
        bottomBar = {
            if (!isEditorActive) {
                RemmiBottomNavigation(
                    navController = navController,
                    runtime = runtime
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(RemmiDestination.HOME.route) {
                HomeScreen(
                    pluginManager = runtime.pluginManager,
                    onWidgetClick = { pluginId ->
                        navController.navigate(
                            RemmiDestination.pluginRoute(pluginId)
                        )
                    }
                )
            }

            composable(RemmiDestination.SETTINGS.route) {
                SettingsScreen(
                    runtime = runtime,
                    navController = navController,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(RemmiDestination.AUTOMATIZATION_ROUTE) {
                AutomatizationSettingsScreen(
                    controller = runtime,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(RemmiDestination.CALENDAR.route) {
                val plugin = runtime.pluginManager.plugins["calendar"]
                if (plugin != null) {
                    plugin.screen.Content(controller = runtime)
                } else {
                    Text("Plugin not found: calendar")
                }
            }

            composable(RemmiDestination.TASKS.route) {
                val plugin = runtime.pluginManager.plugins["tasks"]
                if (plugin != null) {
                    plugin.screen.Content(controller = runtime)
                } else {
                    Text("Plugin not found: tasks")
                }
            }

            composable("plugin/{pluginId}") { backStackEntry ->
                val pluginId = backStackEntry.arguments?.getString("pluginId")
                val plugin = runtime.pluginManager.plugins[pluginId]
                if (plugin != null) {
                    plugin.screen.Content(controller = runtime)
                } else {
                    Text("Plugin not found: $pluginId")
                }
            }
        }
    }
}

@Composable
fun RemmiBottomNavigation(
    navController: NavHostController,
    runtime: RemmiController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val metadata by runtime.pluginManager.pluginMetadata.collectAsState()
    val activePlugins = remember(metadata) {
        metadata
            .filter { it.enabled }
            .mapNotNull { runtime.pluginManager.plugins[it.id] }
    }

    var pluginsOpen by remember { mutableStateOf(false) }

    val navigate: (String) -> Unit = { route ->
        pluginsOpen = false
        navController.navigate(route) {
            popUpTo(RemmiDestination.HOME.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Shared navigation item colors following user's primary color
    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = pluginsOpen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = DesignTokens.SpacingMedium,
                    end = DesignTokens.SpacingMedium,
                    bottom = DesignTokens.BottomNavigationHeight - 4.dp
                ),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PluginMenu(
                plugins = activePlugins,
                onPluginClick = { plugin ->
                    navController.navigate(
                        RemmiDestination.pluginRoute(plugin.metadata.id)
                    ) {
                        launchSingleTop = true
                    }
                    pluginsOpen = false
                }
            )
        }

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.BottomNavigationHeight),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp // Elevation handled by surface container color
        ) {
            NavigationBarItem(
                selected = currentRoute == RemmiDestination.HOME.route,
                onClick = { navigate(RemmiDestination.HOME.route) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = navigationItemColors
            )

            NavigationBarItem(
                selected = currentRoute == RemmiDestination.CALENDAR.route,
                onClick = { navigate(RemmiDestination.CALENDAR.route) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                label = { Text("Calendar") },
                colors = navigationItemColors
            )

            Spacer(modifier = Modifier.size(DesignTokens.IconSizeLarge + 32.dp))

            NavigationBarItem(
                selected = currentRoute == RemmiDestination.TASKS.route,
                onClick = { navigate(RemmiDestination.TASKS.route) },
                icon = { Icon(Icons.Default.Task, contentDescription = "Tasks") },
                label = { Text("Tasks") },
                colors = navigationItemColors
            )

            NavigationBarItem(
                selected = currentRoute == RemmiDestination.SETTINGS.route,
                onClick = { navigate(RemmiDestination.SETTINGS.route) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                colors = navigationItemColors
            )
        }

        FloatingActionButton(
            onClick = { pluginsOpen = !pluginsOpen },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-DesignTokens.SpacingSmall))
                .size(64.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = if (pluginsOpen) Icons.Default.Close else Icons.Default.Extension,
                contentDescription = if (pluginsOpen) "Close plugins" else "Open plugins",
                modifier = Modifier.size(DesignTokens.IconSizeLarge)
            )
        }
    }
}

@Composable
private fun PluginMenu(
    plugins: List<RemmiPlugin>,
    onPluginClick: (RemmiPlugin) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.CornerRadiusLarge),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall)
        ) {
            Text(
                text = "Plugins",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Extend your Remmi system",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DesignTokens.SpacingMedium)
            )

            if (plugins.isEmpty()) {
                Text(
                    text = "No plugins enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                plugins.forEach { plugin ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DesignTokens.CornerRadiusMedium))
                            .clickable { onPluginClick(plugin) }
                            .padding(vertical = DesignTokens.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(plugin.metadata.icon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(DesignTokens.IconSizeMedium)
                            )
                        }

                        Column(modifier = Modifier.padding(start = DesignTokens.SpacingMedium)) {
                            Text(
                                text = plugin.metadata.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v${plugin.metadata.version}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class RemmiDestination(val route: String) {
    data object HOME : RemmiDestination("home")
    data object CALENDAR : RemmiDestination("plugin/calendar")
    data object TASKS : RemmiDestination("plugin/tasks")
    data object SETTINGS : RemmiDestination("settings")
    companion object {
        const val AUTOMATIZATION_ROUTE = "settings/automatization"
        fun pluginRoute(id: String): String = "plugin/$id"
    }
}
