package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.remmi.app.core.ui.navigation.CurvedBottomNavigationView
import com.remmi.app.core.ui.navigation.NavigationItem
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
 */
/**
 * Defines all navigation destinations within the Remmi application.
 *
 * @property route The unique string identifier for the destination.
 */
sealed class RemmiDestination(val route: String) {
    /** The primary dashboard / home screen. */
    data object Home : RemmiDestination("home")
    
    /** The global application settings screen. */
    data object Settings : RemmiDestination("settings")
    
    /** The automation and engine configuration screen. */
    data object Automatization : RemmiDestination("automatization")

    companion object {
        /** Route string for the home screen. */
        const val HOME_ROUTE = "home"
        
        /** Route string for the settings screen. */
        const val SETTINGS_ROUTE = "settings"
        
        /** Route string for the automatization screen. */
        const val AUTOMATIZATION_ROUTE = "automatization"
        
        /** 
         * Generates a route string for a specific plugin screen.
         * @param id The unique identifier of the plugin.
         */
        fun pluginRoute(id: String): String = "plugin/$id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(runtime: RemmiController) {
    /** Controller for managing Jetpack Compose navigation. */
    val navController = rememberNavController()
    
    /** Current state of the navigation backstack. */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    /** The currently active route string. */
    val currentRoute = navBackStackEntry?.destination?.route
    
    /** List of all installed plugins' metadata. */
    val metadata by runtime.pluginManager.pluginMetadata.collectAsState()
    
    /** Filtered list of currently enabled plugin instances. */
    val activePlugins = remember(metadata) {
        metadata.filter { it.enabled }.mapNotNull { runtime.pluginManager.plugins[it.id] }
    }

    val isEditorActive = GlobalUIState.isEditorActive.value
    val isMenuVisible = GlobalUIState.isMenuVisible.value
    val isSettingsRoute = currentRoute == RemmiDestination.SETTINGS_ROUTE
    
    var showDashboard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !isEditorActive && isMenuVisible && !isSettingsRoute,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                CurvedNavigationWrapper(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(RemmiDestination.HOME_ROUTE) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenDashboard = { showDashboard = true }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RemmiDestination.HOME_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
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
            
            if (showDashboard) {
                ModalBottomSheet(
                    onDismissRequest = { showDashboard = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    PluginDashboard(
                        activePlugins = activePlugins,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(RemmiDestination.HOME_ROUTE) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            showDashboard = false
                        }
                    )
                }
            }
        }
        GlobalUIOverlays(runtime, scope)
    }

@Composable
fun CurvedNavigationWrapper(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onOpenDashboard: () -> Unit
) {
    val items = remember {
        listOf(
            NavigationItem(com.remmi.app.R.drawable.ic_nav_home_alt, "Home"),
            NavigationItem(com.remmi.app.R.drawable.ic_nav_calendar, "Calendar"),
            NavigationItem(com.remmi.app.R.drawable.ic_nav_tasks, "Tasks"),
            NavigationItem(com.remmi.app.R.drawable.ic_nav_settings_alt, "Settings")
        )
    }

    val selectedIndex = remember(currentRoute) {
        when {
            currentRoute == RemmiDestination.HOME_ROUTE -> 0
            currentRoute?.contains("calendar") == true -> 1
            currentRoute?.contains("tasks") == true -> 2
            currentRoute == RemmiDestination.SETTINGS_ROUTE -> 3
            else -> 0
        }
    }

    val navBarColor = MaterialTheme.colorScheme.surface.toArgb()
    val centerButtonColor = MaterialTheme.colorScheme.surface.toArgb()
    val selectedIconColor = MaterialTheme.colorScheme.primary.toArgb()
    val unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f).toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Respect system bars
            .padding(bottom = 24.dp) // Move up higher
            .height(110.dp), // Slightly taller to accommodate curves and centering
        factory = { context ->
            CurvedBottomNavigationView(context).apply {
                setItems(items)
                setOnItemSelectedListener { index ->
                    val route = when (index) {
                        0 -> RemmiDestination.HOME_ROUTE
                        1 -> RemmiDestination.pluginRoute("calendar")
                        2 -> RemmiDestination.pluginRoute("tasks")
                        3 -> RemmiDestination.SETTINGS_ROUTE
                        else -> RemmiDestination.HOME_ROUTE
                    }
                    onNavigate(route)
                }
                setOnCenterActionClickListener {
                    onOpenDashboard()
                }
            }
        },
        update = { view ->
            view.setSelectedIndex(selectedIndex)
            view.updateColors(
                navBarColor = navBarColor,
                centerButtonColor = centerButtonColor,
                selectedIconColor = selectedIconColor,
                unselectedIconColor = unselectedIconColor
            )
        }
    )
}

@Composable
fun PluginDashboard(
    activePlugins: List<RemmiPlugin>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(activePlugins, key = { it.metadata.id }) { plugin ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = plugin.metadata.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onNavigate(RemmiDestination.pluginRoute(plugin.metadata.id))
                        }
                ) {
                    plugin.widget.Content()
                }
            }
        }
    }
}


@Composable
fun GlobalUIOverlays(runtime: RemmiController, scope: kotlinx.coroutines.CoroutineScope) {
    if (GlobalUIState.showLocationPicker.value) {
        val mapsPlugin = runtime.pluginManager.plugins["maps"] as? com.remmi.app.plugins.maps.MapsPlugin
        val data = GlobalUIState.locationPickerData.value
        if (mapsPlugin != null && data != null) {
            com.remmi.app.plugins.maps.ui.popups.LocationPickerPopup(
                actions = mapsPlugin.actions,
                controller = runtime,
                requestId = data.sourceItemId,
                initialSearch = data.title,
                correlationId = data.correlationId,
                onDismiss = { GlobalUIState.showLocationPicker.value = false }
            )
        }
    }
    GlobalUIState.pendingAlarmRequest.value?.let { data ->
        com.remmi.app.plugins.alarm.popups.AlarmConfigurationDialog(
            data = data,
            onDismiss = { GlobalUIState.pendingAlarmRequest.value = null },
            onConfirm = { command ->
                scope.launch {
                    GlobalUIState.lastConfirmedCorrelationId.value = data.correlationId
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
                    GlobalUIState.lastConfirmedCorrelationId.value = data.correlationId
                    runtime.eventBus.publishCommand(command)
                    GlobalUIState.pendingTaskRequest.value = null
                }
            }
        )
    }

    // Receipt Image Acquisition
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val request = GlobalUIState.pendingReceiptImageRequest.value
        if (uri != null && request != null) {
            scope.launch {
                runtime.eventBus.publishEvent(
                    com.remmi.app.core.eventBus.events.ReceiptImageSelectedEvent(
                        imageUri = uri.toString(),
                        requestId = request.requestId
                    )
                )
            }
        }
        GlobalUIState.pendingReceiptImageRequest.value = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val request = GlobalUIState.pendingReceiptImageRequest.value
        val uri = cameraTempUri
        if (success && uri != null && request != null) {
            scope.launch {
                runtime.eventBus.publishEvent(
                    com.remmi.app.core.eventBus.events.ReceiptImageSelectedEvent(
                        imageUri = uri.toString(),
                        requestId = request.requestId
                    )
                )
            }
        }
        GlobalUIState.pendingReceiptImageRequest.value = null
    }

    LaunchedEffect(Unit) {
        runtime.eventBus.commands.collect { command ->
            if (command is com.remmi.app.core.eventBus.commands.RequestReceiptImageCommand) {
                GlobalUIState.pendingReceiptImageRequest.value = com.remmi.app.core.controller.ReceiptImageData(
                    requestId = command.requestId,
                    useCamera = command.useCamera
                )
                if (command.useCamera) {
                    val file = java.io.File(runtime.androidContext.cacheDir, "receipt_temp.jpg")
                    if (file.exists()) file.delete()
                    val uri = FileProvider.getUriForFile(
                        runtime.androidContext,
                        "com.remmi.app.fileprovider",
                        file
                    )
                    cameraTempUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    galleryLauncher.launch("image/*")
                }
            }
        }
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
