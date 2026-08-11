package com.remmi.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.HomeScreen

sealed class RemmiDestination(val route: String) {
    data object Home : RemmiDestination("home")
    data object Settings : RemmiDestination("settings")

    companion object {
        const val HOME_ROUTE = "home"
        const val SETTINGS_ROUTE = "settings"
        fun pluginRoute(id: String) = "plugin/$id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(context: PluginContext) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val plugins = context.pluginManager.plugins.values.toList()
    
    var showAllPluginsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -20) { // Swipe up
                                showAllPluginsSheet = true
                            }
                        }
                    }
            ) {
                // Visual handle to indicate swipeable area
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                NavigationBar {
                    // Home
                    NavigationBarItem(
                        selected = currentRoute == RemmiDestination.HOME_ROUTE,
                        onClick = { navController.navigate(RemmiDestination.HOME_ROUTE) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )

                    // Dynamic Plugin Items (those marked for navigation)
                    // Limit to 3 items to avoid overcrowding
                    plugins.filter { it.metadata.showInNavigation }.take(3).forEach { plugin ->
                        val route = RemmiDestination.pluginRoute(plugin.metadata.id)
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = { navController.navigate(route) },
                            icon = {
                                Icon(
                                    imageVector = getIconForName(plugin.metadata.icon),
                                    contentDescription = plugin.metadata.name
                                )
                            },
                            label = { Text(plugin.metadata.name) }
                        )
                    }

                    // Settings
                    NavigationBarItem(
                        selected = currentRoute == RemmiDestination.SETTINGS_ROUTE,
                        onClick = { navController.navigate(RemmiDestination.SETTINGS_ROUTE) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    )
{ paddingValues ->
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME_ROUTE,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(RemmiDestination.HOME_ROUTE) {
                HomeScreen(context.widgetManager)
            }

            // Dynamically register all plugin routes
            plugins.forEach { plugin ->
                composable(RemmiDestination.pluginRoute(plugin.metadata.id)) {
                    plugin.screen.Content()
                }
            }

            composable(RemmiDestination.SETTINGS_ROUTE) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Settings")
                }
            }
        }

        if (showAllPluginsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAllPluginsSheet = false },
                sheetState = sheetState
            ) {
                PluginGrid(
                    plugins = plugins,
                    onPluginClick = { pluginId ->
                        navController.navigate(RemmiDestination.pluginRoute(pluginId))
                        showAllPluginsSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun PluginGrid(
    plugins: List<RemmiPlugin>,
    onPluginClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp) // Extra padding for the handle/safe area
    ) {
        Text(
            text = "Your Plugins",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(plugins) { plugin ->
                PluginGridItem(plugin, onPluginClick)
            }
        }
    }
}

@Composable
fun PluginGridItem(
    plugin: RemmiPlugin,
    onClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick(plugin.metadata.id) }
            .padding(8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getIconForName(plugin.metadata.icon),
                    contentDescription = plugin.metadata.name,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = plugin.metadata.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1
        )
    }
}

fun getIconForName(name: String?): ImageVector {
    return when (name) {
        "calendar_month" -> Icons.Default.CalendarMonth
        "check_circle" -> Icons.Default.CheckCircle
        "alarm" -> Icons.Default.Alarm
        "settings" -> Icons.Default.Settings
        "home" -> Icons.Default.Home
        "person" -> Icons.Default.Person
        "card_giftcard" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Extension
    }
}
