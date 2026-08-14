package com.remmi.app.core.navigation

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.runtime.RemmiCore
import com.remmi.app.core.screens.HomeScreen
import com.remmi.app.core.screens.SettingsScreen
import kotlinx.coroutines.launch

sealed class RemmiDestination(val route: String) {
    init {
        Log.d("Remmi", "[RemmiDestination] - [constructor] executed")
    }
    data object Home : RemmiDestination("home")
    data object Settings : RemmiDestination("settings")

    companion object {
        const val HOME_ROUTE = "home"
        const val SETTINGS_ROUTE = "settings"
        fun pluginRoute(id: String): String {
            Log.d("Remmi", "[RemmiDestination] - [pluginRoute] executed")
            return "plugin/$id"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(runtime: RemmiCore) {
    Log.d("Remmi", "[AppNavigation] - [AppNavigation] executed")
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val metadata by runtime.pluginManager.pluginMetadata.collectAsState()
    val activePlugins = remember(metadata) {
        metadata.filter { it.enabled }.mapNotNull { runtime.pluginManager.plugins[it.id] }
    }
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 96.dp,
        sheetDragHandle = null,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f) // Reach nearly to the top
                    .padding(bottom = 16.dp)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(96.dp).padding(top = 16.dp)
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentRoute == RemmiDestination.HOME_ROUTE,
                        onClick = { 
                            navController.navigate(RemmiDestination.HOME_ROUTE)
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )

                    // Dynamic Plugin Items: Calendar and Tasks only
                    metadata.filter { it.enabled && (it.id == "calendar" || it.id == "tasks") }.forEach { pluginMeta ->
                        val route = RemmiDestination.pluginRoute(pluginMeta.id)
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = { 
                                navController.navigate(route)
                                scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                            },
                            icon = {
                                Icon(
                                    imageVector = getIconForName(pluginMeta.icon),
                                    contentDescription = pluginMeta.name
                                )
                            },
                            label = { Text(pluginMeta.name) }
                        )
                    }

                    // Settings
                    NavigationBarItem(
                        selected = currentRoute == RemmiDestination.SETTINGS_ROUTE,
                        onClick = { 
                            navController.navigate(RemmiDestination.SETTINGS_ROUTE)
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                
                PluginGrid(
                    plugins = activePlugins,
                    onPluginClick = { pluginId ->
                        navController.navigate(RemmiDestination.pluginRoute(pluginId))
                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME_ROUTE,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(RemmiDestination.HOME_ROUTE) {
                HomeScreen(
                    pluginManager = runtime.pluginManager,
                    onWidgetClick = { pluginId ->
                        navController.navigate(RemmiDestination.pluginRoute(pluginId))
                    }
                )
            }

            // Dynamically register enabled plugin routes
            activePlugins.forEach { plugin ->
                composable(RemmiDestination.pluginRoute(plugin.metadata.id)) {
                    plugin.screen.Content()
                }
            }

            composable(RemmiDestination.SETTINGS_ROUTE) {
                SettingsScreen(
                    runtime = runtime,
                    onBack = {
                        navController.navigate(RemmiDestination.HOME_ROUTE) {
                            popUpTo(RemmiDestination.HOME_ROUTE) { inclusive = true }
                        }
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
    Log.d("Remmi", "[AppNavigation] - [PluginGrid] executed")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
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
    Log.d("Remmi", "[AppNavigation] - [PluginGridItem] executed")
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
    Log.d("Remmi", "[AppNavigation] - [getIconForName] executed")
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
