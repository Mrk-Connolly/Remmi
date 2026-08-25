package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.controller.GlobalUIState
import kotlinx.coroutines.launch

/**
 * REMMI DESTINATION
 * Sealed class defining the app's navigation routes
 */
sealed class RemmiDestination(val route: String) {

    init {
        Log.d("Remmi", "[Remmi Destination] - Constructor initialized")
    }

    data object Home : RemmiDestination("home")
    data object Settings : RemmiDestination("settings")
    data object Automatization : RemmiDestination("automatization")

    companion object {
        const val HOME_ROUTE = "home"
        const val SETTINGS_ROUTE = "settings"
        const val AUTOMATIZATION_ROUTE = "automatization"

        fun pluginRoute(id: String): String {
            return "plugin/$id"
        }
    }
}

/**
 * APP NAVIGATION
 * Main navigation controller for the Remmi application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(runtime: RemmiController) {
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

    // Animation state for switching between Island and Full Menu
    val targetState = scaffoldState.bottomSheetState.targetValue
    val fullMenuAlpha by animateFloatAsState(if (targetState == SheetValue.Expanded) 1f else 0f, label = "fullAlpha")
    val islandAlpha by animateFloatAsState(if (targetState == SheetValue.Expanded) 0f else 1f, label = "islandAlpha")

    val horizontalPadding by animateDpAsState(if (targetState == SheetValue.Expanded) 0.dp else 24.dp, label = "hPadding")
    val bottomPadding by animateDpAsState(if (targetState == SheetValue.Expanded) 0.dp else 48.dp, label = "bPadding")
    val cornerRadius by animateDpAsState(if (targetState == SheetValue.Expanded) 0.dp else 12.dp, label = "cornerRadius")

    val isCalendarRoute = currentRoute?.contains("calendar") == true
    val isSettingsRoute = currentRoute == RemmiDestination.SETTINGS_ROUTE

    val isEditorActive = GlobalUIState.isEditorActive.value
    val isMenuVisible = GlobalUIState.isMenuVisible.value
    
    val animatedPeekHeight by animateDpAsState(
        if (isEditorActive || !isMenuVisible || isSettingsRoute) 0.dp else 160.dp,
        label = "peekHeight"
    )

    // Listen for Map Commands
    LaunchedEffect(Unit) {
        runtime.eventBus.commands.collect { command ->
            if (command is com.remmi.app.core.eventBus.commands.PickLocationCommand) {
                val mapsPlugin = runtime.pluginManager.plugins["maps"] as? com.remmi.app.plugins.maps.MapPlugin
                mapsPlugin?.handleCommandWithController(command, runtime)
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = animatedPeekHeight,
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(0.dp),
        sheetContainerColor = Color.Transparent,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        sheetContent = {
            if (animatedPeekHeight > 0.dp) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Large Full Plugin Menu (Square/Full Screen)
                    if (fullMenuAlpha > 0f) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(fullMenuAlpha),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                                Text(
                                    text = "All Plugins",
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                PluginGrid(
                                    plugins = activePlugins,
                                    onPluginClick = { pluginId ->
                                        navController.navigate(RemmiDestination.pluginRoute(pluginId))
                                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                    }
                                )
                            }
                        }
                    }

                    // Small Floating Island Menu (Rectangle)
                    if (islandAlpha > 0f) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter) // Align to top of the peek area
                                .padding(top = 24.dp) // Gap to match bottom (24dp)
                                .padding(horizontal = horizontalPadding)
                                .padding(bottom = bottomPadding)
                                .fillMaxWidth()
                                .alpha(islandAlpha),
                            shape = RoundedCornerShape(cornerRadius),
                            tonalElevation = 6.dp,
                            shadowElevation = 6.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth(),
                                    windowInsets = WindowInsets(0)
                                ) {
                                    IslandNavigationItems(
                                        currentRoute = currentRoute,
                                        navController = navController,
                                        metadata = metadata,
                                        onNavigate = { scope.launch { scaffoldState.bottomSheetState.partialExpand() } }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Return an empty box when hidden to prevent touch interception
                Box(Modifier.size(0.dp))
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            composable(RemmiDestination.HOME_ROUTE) {
                HomeScreen(
                    pluginManager = runtime.pluginManager,
                    onWidgetClick = { pluginId ->
                        navController.navigate(RemmiDestination.pluginRoute(pluginId))
                    }
                )
            }

            activePlugins.forEach { plugin ->
                composable(RemmiDestination.pluginRoute(plugin.metadata.id)) {
                    plugin.screen.Content(controller = runtime)
                }
            }

            composable(RemmiDestination.SETTINGS_ROUTE) {
                SettingsScreen(
                    runtime = runtime,
                    navController = navController,
                    onBack = {
                        navController.navigate(RemmiDestination.HOME_ROUTE) {
                            popUpTo(RemmiDestination.HOME_ROUTE) { inclusive = true } }
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
        }

        // Global Overlays
        if (GlobalUIState.showLocationPicker.value) {
            val mapsPlugin = runtime.pluginManager.plugins["maps"] as? com.remmi.app.plugins.maps.MapPlugin
            if (mapsPlugin != null) {
                com.remmi.app.plugins.maps.popups.LocationPickerPopup(
                    actions = mapsPlugin.actions,
                    controller = runtime,
                    requestId = GlobalUIState.locationPickerRequestId.value,
                    initialSearch = GlobalUIState.locationPickerInitialSearch.value,
                    onDismiss = { GlobalUIState.showLocationPicker.value = false }
                )
            }
        }

        // Pending Linked Creation Dialogs
        GlobalUIState.pendingAlarmRequest.value?.let { data ->
            com.remmi.app.plugins.alarm.popups.AlarmConfigurationDialog(
                data = data,
                onDismiss = { GlobalUIState.pendingAlarmRequest.value = null },
                onConfirm = { command ->
                    scope.launch {
                        runtime.eventBus.publishCommand(command)
                        GlobalUIState.pendingAlarmRequest.value = null
                    }
                }
            )
        }

        GlobalUIState.pendingTaskRequest.value?.let { data ->
            com.remmi.app.plugins.tasks.popups.TaskConfigurationDialog(
                data = data,
                onDismiss = { GlobalUIState.pendingTaskRequest.value = null },
                onConfirm = { command ->
                    scope.launch {
                        runtime.eventBus.publishCommand(command)
                        GlobalUIState.pendingTaskRequest.value = null
                    }
                }
            )
        }
    }
}

@Composable
fun RowScope.IslandNavigationItems(
    currentRoute: String?,
    navController: NavController,
    metadata: List<PluginMetadata>,
    onNavigate: () -> Unit
) {
    // Hardcoded Island Menu Items (Home, Calendar, Tasks, Settings)
    NavigationBarItem(
        selected = currentRoute == RemmiDestination.HOME_ROUTE,
        onClick = { 
            navController.navigate(RemmiDestination.HOME_ROUTE)
            onNavigate()
        },
        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
        label = { Text("Home") }
    )

    val isCalendarEnabled = metadata.any { it.id == "calendar" && it.enabled }
    if (isCalendarEnabled) {
        NavigationBarItem(
            selected = currentRoute?.contains("calendar") == true,
            onClick = { 
                navController.navigate(RemmiDestination.pluginRoute("calendar"))
                onNavigate()
            },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
    }

    val isTasksEnabled = metadata.any { it.id == "tasks" && it.enabled }
    if (isTasksEnabled) {
        NavigationBarItem(
            selected = currentRoute?.contains("tasks") == true,
            onClick = { 
                navController.navigate(RemmiDestination.pluginRoute("tasks"))
                onNavigate()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tasks") },
            label = { Text("Tasks") }
        )
    }

    val isMapsEnabled = metadata.any { it.id == "maps" && it.enabled }
    if (isMapsEnabled) {
        NavigationBarItem(
            selected = currentRoute?.contains("maps") == true,
            onClick = { 
                navController.navigate(RemmiDestination.pluginRoute("maps"))
                onNavigate()
            },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map") }
        )
    }

    NavigationBarItem(
        selected = currentRoute == RemmiDestination.SETTINGS_ROUTE,
        onClick = { 
            navController.navigate(RemmiDestination.SETTINGS_ROUTE)
            onNavigate()
        },
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        label = { Text("Settings") }
    )
}

@Composable
fun ThreeDotsDragHandle() {
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
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
        "restaurant" -> Icons.Default.Restaurant
        "kitchen" -> Icons.Default.Kitchen
        "wb_sunny" -> Icons.Default.WbSunny
        "map" -> Icons.Default.Map
        else -> Icons.Default.Extension
    }
}
