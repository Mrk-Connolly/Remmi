package com.remmi.app.core.navigation

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.auth.AuthState
import com.remmi.app.core.auth.AuthViewModel
import com.remmi.app.core.screens.AuthScreen
import com.remmi.app.core.screens.HomeScreen
import com.remmi.app.core.screens.SettingsScreen
import com.remmi.app.core.screens.AutomatizationSettingsScreen
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

    Log.d("Remmi", "[AppNavigation] - [AppNavigation] executed")
    val authState by runtime.authRepository.sessionStatus.collectAsState(initial = AuthState.Loading)

    when (authState) {
        AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            // Bypass Auth for Testing
            MainAppContent(runtime)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(runtime: RemmiController) {
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

    val isEditorActive = runtime.isEditorActive.value
    val isMenuVisible = runtime.isMenuVisible.value
    
    val animatedPeekHeight by animateDpAsState(
        if (isEditorActive || !isMenuVisible || isSettingsRoute) 0.dp else 160.dp,
        label = "peekHeight"
    )

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
    }
}

@Composable
fun RowScope.IslandNavigationItems(
    currentRoute: String?,
    navController: androidx.navigation.NavController,
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

    NavigationBarItem(
        selected = currentRoute?.contains("calendar") == true,
        onClick = { 
            navController.navigate(RemmiDestination.pluginRoute("calendar"))
            onNavigate()
        },
        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
        label = { Text("Calendar") }
    )

    NavigationBarItem(
        selected = currentRoute?.contains("tasks") == true,
        onClick = { 
            navController.navigate(RemmiDestination.pluginRoute("tasks"))
            onNavigate()
        },
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tasks") },
        label = { Text("Tasks") }
    )

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
        else -> Icons.Default.Extension
    }
}
