package com.remmi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
@Composable
fun AppNavigation(
    runtime: RemmiController
) {
    val navController = rememberNavController()
    val isEditorActive by GlobalUIState.isEditorActive
    var pluginsOpen by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (!isEditorActive) {
                RemmiBottomNavigation(
                    navController = navController,
                    runtime = runtime,
                    pluginsOpen = pluginsOpen,
                    onPluginsOpenChange = { pluginsOpen = it }
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

    if (pluginsOpen && !isEditorActive) {
        PluginBottomSheet(
            runtime = runtime,
            onDismiss = { pluginsOpen = false },
            onPluginClick = { plugin ->
                pluginsOpen = false
                navController.navigate(
                    RemmiDestination.pluginRoute(plugin.metadata.id)
                ) {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
fun RemmiBottomNavigation(
    navController: NavHostController,
    runtime: RemmiController,
    pluginsOpen: Boolean,
    onPluginsOpenChange: (Boolean) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val metadata by runtime.pluginManager.pluginMetadata.collectAsState()
    val activePlugins = remember(metadata) {
        metadata
            .filter { it.enabled }
            .mapNotNull { runtime.pluginManager.plugins[it.id] }
    }

    val navigate: (String) -> Unit = { route ->
        onPluginsOpenChange(false)
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(RemmiDestination.HOME.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

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
            .padding(horizontal = 16.dp, vertical = 12.dp) // Add padding for floating effect
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(DesignTokens.BottomNavigationHeight),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = RoundedCornerShape(DesignTokens.CornerRadiusLarge),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            tonalElevation = 0.dp
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent, // Managed by Surface
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == RemmiDestination.HOME.route,
                    onClick = { 
                        if (currentRoute != RemmiDestination.HOME.route) {
                            navController.popBackStack(RemmiDestination.HOME.route, inclusive = false)
                        }
                    },
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
        }

        FloatingActionButton(
            onClick = { onPluginsOpenChange(!pluginsOpen) },
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
                imageVector = if (pluginsOpen) Icons.Default.Close else Icons.Default.Apps,
                contentDescription = if (pluginsOpen) "Close plugins" else "Open plugins",
                modifier = Modifier.size(DesignTokens.IconSizeLarge)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginBottomSheet(
    runtime: RemmiController,
    onDismiss: () -> Unit,
    onPluginClick: (RemmiPlugin) -> Unit
) {
    val metadata by runtime.pluginManager.pluginMetadata.collectAsState()
    val activePlugins = remember(metadata) {
        metadata
            .filter { it.enabled }
            .mapNotNull { runtime.pluginManager.plugins[it.id] }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(),
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        shape = RoundedCornerShape(
            topStart = DesignTokens.CornerRadiusLarge,
            topEnd = DesignTokens.CornerRadiusLarge
        )
    ) {
        PluginMenu(
            plugins = activePlugins,
            onPluginClick = onPluginClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PluginMenu(
    plugins: List<RemmiPlugin>,
    onPluginClick: (RemmiPlugin) -> Unit
) {
    val groupedPlugins = remember(plugins) {
        plugins.groupBy { plugin ->
            pluginGroupFor(plugin.metadata.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = DesignTokens.SpacingLarge,
                end = DesignTokens.SpacingLarge,
                bottom = DesignTokens.SpacingLarge
            ),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingLarge)
    ) {
        Column {
            Text(
                text = "Plugins",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weather Quick Access
                val weather = plugins.find { it.metadata.id == "weather" }
                if (weather != null) {
                    QuickAccessButton(
                        name = "Weather",
                        icon = "wb_sunny",
                        modifier = Modifier.weight(1f),
                        onClick = { onPluginClick(weather) }
                    )
                }

                // Call Recorder Quick Access
                val recorder = plugins.find { it.metadata.id == "call_recorder" }
                if (recorder != null) {
                    QuickAccessButton(
                        name = "Recorder",
                        icon = "mic",
                        modifier = Modifier.weight(1f),
                        onClick = { onPluginClick(recorder) }
                    )
                }
            }
        }

        if (plugins.isEmpty()) {
            Text(
                text = "No plugins enabled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = DesignTokens.SpacingMedium)
            )
        } else {
            groupedPlugins
                .toSortedMap(compareBy { it.ordinal })
                .forEach { (group, groupPlugins) ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)
                    ) {
                        Text(
                            text = group.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                DesignTokens.SpacingMedium
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                DesignTokens.SpacingMedium
                            ),
                            maxItemsInEachRow = 3
                        ) {
                            groupPlugins.forEach { plugin ->
                                Column(
                                    modifier = Modifier
                                        .width(84.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                DesignTokens.CornerRadiusMedium
                                            )
                                        )
                                        .clickable { onPluginClick(plugin) }
                                        .padding(vertical = DesignTokens.SpacingSmall),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.1f
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconForName(plugin.metadata.icon),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(
                                                DesignTokens.IconSizeLarge
                                            )
                                        )
                                    }

                                    Text(
                                        text = plugin.metadata.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun QuickAccessButton(
    name: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconForName(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class PluginGroup(
    val displayName: String
) {
    PRODUCTIVITY("Productivity"),
    COMMUNICATION("Communication"),
    INFORMATION("Information"),
    AUTOMATION("Automation"),
    OTHER("Other")
}

private fun pluginGroupFor(pluginId: String): PluginGroup =
    when (pluginId.lowercase().trim()) {
        "calendar", "tasks", "weather", "call_recorder", "recorder", "notes", "reminders", "todo", "todos" ->
            PluginGroup.PRODUCTIVITY

        "messages", "messaging", "email", "mail", "contacts" ->
            PluginGroup.COMMUNICATION

        "news", "rss", "search" ->
            PluginGroup.INFORMATION

        "automation", "automations", "automatization", "workflows" ->
            PluginGroup.AUTOMATION

        else -> PluginGroup.OTHER
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
