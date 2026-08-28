package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
 */
sealed class RemmiDestination(val route: String) {
    data object Home : RemmiDestination("home")
    data object Settings : RemmiDestination("settings")
    data object Automatization : RemmiDestination("automatization")

    companion object {
        const val HOME_ROUTE = "home"
        const val SETTINGS_ROUTE = "settings"
        const val AUTOMATIZATION_ROUTE = "automatization"
        fun pluginRoute(id: String): String = "plugin/$id"
    }
}

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
                UnifiedNavigationBar(
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = RemmiDestination.HOME_ROUTE,
                modifier = Modifier.fillMaxSize().padding(bottom = if (!isEditorActive && isMenuVisible && !isSettingsRoute) 0.dp else 0.dp) // Scaffold handles padding
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
                    tonalElevation = 8.dp
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
}

@Composable
fun UnifiedNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onOpenDashboard: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItemIconCompact(
                selected = currentRoute == RemmiDestination.HOME_ROUTE,
                icon = Icons.Default.Home,
                label = "Home",
                onClick = { onNavigate(RemmiDestination.HOME_ROUTE) }
            )
            DockItemIconCompact(
                selected = currentRoute?.contains("calendar") == true,
                icon = Icons.Default.CalendarMonth,
                label = "Calendar",
                onClick = { onNavigate(RemmiDestination.pluginRoute("calendar")) }
            )
            
            // Central Dashboard Trigger
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onOpenDashboard() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Dashboard,
                    contentDescription = "Dashboard",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            DockItemIconCompact(
                selected = currentRoute?.contains("tasks") == true,
                icon = Icons.Default.CheckCircle,
                label = "Tasks",
                onClick = { onNavigate(RemmiDestination.pluginRoute("tasks")) }
            )
            DockItemIconCompact(
                selected = currentRoute == RemmiDestination.SETTINGS_ROUTE,
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = { onNavigate(RemmiDestination.SETTINGS_ROUTE) }
            )
        }
    }
}

@Composable
fun PluginDashboard(
    activePlugins: List<RemmiPlugin>,
    onNavigate: (String) -> Unit
) {
    val groupedPlugins = remember(activePlugins) {
        activePlugins.groupBy { it.metadata.group }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f) // Don't take full screen
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
        groupedPlugins.forEach { (group, plugins) ->
            item {
                Text(
                    text = group.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            item {
                PluginGridRowsExpanded(plugins, onPluginClick = { onNavigate(RemmiDestination.pluginRoute(it)) })
            }
        }
    }
}

@Composable
fun DockItemIconCompact(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PluginGridRowsExpanded(
    plugins: List<RemmiPlugin>,
    onPluginClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val rows = plugins.chunked(3)
        rows.forEach { rowPlugins ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowPlugins.forEach { plugin ->
                    Box(modifier = Modifier.weight(1f)) {
                        ExpandedPluginItem(plugin, onPluginClick)
                    }
                }
                repeat(3 - rowPlugins.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExpandedPluginItem(
    plugin: RemmiPlugin,
    onClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(plugin.metadata.id) }
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
