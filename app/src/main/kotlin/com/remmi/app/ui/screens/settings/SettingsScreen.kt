package com.remmi.app.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.RemmiSectionHeader
import com.remmi.app.ui.components.RemmiSecondaryScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.remmi.app.core.controller.RemmiThemeMode
import com.remmi.app.core.memory.MemoryProviderType
import com.remmi.app.core.memory.migration.TableMigrationSpec
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.ui.components.RemmiDestination
import com.remmi.app.ui.components.getIconForName
import com.remmi.app.ui.components.HueRingPicker
import com.remmi.app.ui.PrimaryPalette
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.remmi.app.core.eventBus.commands.SetCallRecordingModeCommand

/**
 * SETTINGS SCREEN
 * Configuration page for plugin management and system settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    runtime: RemmiController,
    navController: NavHostController,
    onBack: () -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    Log.d("Remmi", "[SettingsScreen] - [SettingsScreen] executed")
    val pluginManager = runtime.pluginManager
    val metadata by pluginManager.pluginMetadata.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    
    var pendingMetadata by remember(metadata) { mutableStateOf(metadata) }
    val hasChanges = remember(metadata, pendingMetadata) { metadata != pendingMetadata }

    var selectedPluginForInfo by remember { mutableStateOf<PluginMetadata?>(null) }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Refresh
     * Refresh settings data
     * */
    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                // In a real app, this might reload settings from disk or server
                delay(500)
                isRefreshing = false
            }
        }
    }

    RemmiSecondaryScreen(
        title = "Settings",
        onBack = onBack,
        topBarActions = {
            if (hasChanges) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pluginManager.updateAllPluginSettings(runtime.androidManager.fileService, pendingMetadata)
                            pluginManager.loadPlugins()
                            onBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Apply Changes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                contentPadding = PaddingValues(
                    start = DesignTokens.SpacingLarge,
                    end = DesignTokens.SpacingLarge,
                    top = DesignTokens.SpacingMedium,
                    bottom = 100.dp
                )
            ) {
                item {
                    RemmiSectionHeader(title = "Appearance")
                }

                item {
                    AppearanceSettings(runtime)
                }

                item {
                    RemmiSectionHeader(title = "Memory & Storage")
                }

                item {
                    MemoryStorageSettings(runtime)
                }

                item {
                    RemmiSectionHeader(title = "System Features")
                }

                item {
                    CallRecordingSettings(runtime)
                }

                item {
                    RemmiCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(RemmiDestination.AUTOMATIZATION_ROUTE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(DesignTokens.SpacingMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(DesignTokens.SpacingMedium))
                            Text(text = "Daily Briefing & Automations", modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                item {
                    RemmiCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(RemmiDestination.LAUNCHER_SETTINGS.route) }
                    ) {
                        Row(
                            modifier = Modifier.padding(DesignTokens.SpacingMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(DesignTokens.SpacingMedium))
                            Text(text = "Launcher & Personal Dashboard", modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                item {
                    RemmiSectionHeader(title = "Plugin Management")
                }

                items(pendingMetadata) { plugin ->
                    val onToggleEnabled = remember(plugin.id) {
                        { enabled: Boolean ->
                            pendingMetadata = pendingMetadata.map {
                                if (it.id == plugin.id) it.copy(enabled = enabled) else it
                            }
                        }
                    }
                    val onToggleNavigation = remember(plugin.id) {
                        { show: Boolean ->
                            pendingMetadata = pendingMetadata.map {
                                if (it.id == plugin.id) it.copy(showInNavigation = show) else it
                            }
                        }
                    }
                    val onToggleWidget = remember(plugin.id) {
                        { show: Boolean ->
                            pendingMetadata = pendingMetadata.map {
                                if (it.id == plugin.id) it.copy(showWidget = show) else it
                            }
                        }
                    }

                    PluginSettingItem(
                        plugin = plugin,
                        onToggleEnabled = onToggleEnabled,
                        onToggleNavigation = onToggleNavigation,
                        onToggleWidget = onToggleWidget,
                        onLongClick = { selectedPluginForInfo = plugin }
                    )
                }
            }
        }
    }

    selectedPluginForInfo?.let { plugin ->
        AlertDialog(
            onDismissRequest = { selectedPluginForInfo = null },
            title = { Text("Plugin Information") },
            text = {
                Column {
                    Text("Name: ${plugin.name}")
                    Text("ID: ${plugin.id}")
                    Text("Version: ${plugin.version}")
                    Text("Author: ${plugin.author}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPluginForInfo = null }) {
                    Text("Close")
                }
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }
}

/**
 * CALL RECORDING SETTINGS
 */
@Composable
fun CallRecordingSettings(runtime: RemmiController) {
    val settings = runtime.androidManager.settingsService
    val scope = rememberCoroutineScope()
    
    var recordingMode by remember {
        mutableStateOf(settings.getString("call_recording_mode", "ASK_EVERY_CALL") ?: "ASK_EVERY_CALL")
    }

    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)) {
        RemmiCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(DesignTokens.SpacingLarge)) {
                Text(
                    "Call Recording",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(DesignTokens.SpacingSmall))
                
                listOf(
                    "ASK_EVERY_CALL" to "Ask every call",
                    "ALWAYS_RECORD" to "Automatically record",
                    "NEVER_RECORD" to "Never record"
                ).forEach { (mode, label) ->
                    val isSelected = recordingMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                recordingMode = mode
                                settings.setString("call_recording_mode", mode)
                                scope.launch {
                                    runtime.eventBus.publishCommand(SetCallRecordingModeCommand(mode))
                                }
                            }
                            .padding(vertical = DesignTokens.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = {
                            recordingMode = mode
                            settings.setString("call_recording_mode", mode)
                            scope.launch {
                                runtime.eventBus.publishCommand(SetCallRecordingModeCommand(mode))
                            }
                        })
                        Column {
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            if (mode == "ASK_EVERY_CALL") {
                                Text(
                                    "Remmi will ask before recording each call",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * APPEARANCE SETTINGS
 */
@Composable
fun AppearanceSettings(runtime: RemmiController) {
    RemmiCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(DesignTokens.SpacingLarge)) {
            Text(
                "Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(DesignTokens.SpacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall)
            ) {
                listOf(
                    RemmiThemeMode.LIGHT to "Light",
                    RemmiThemeMode.DARK to "Dark",
                    RemmiThemeMode.SYSTEM to "System"
                ).forEach { (mode, label) ->
                    val isSelected = GlobalUIState.themePreference == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                GlobalUIState.themePreference = mode
                                runtime.androidManager.settingsService.setString("theme_pref", mode.name)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            var showColorPicker by remember { mutableStateOf(false) }

            Spacer(Modifier.height(DesignTokens.SpacingLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Theme Colour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(android.graphics.Color.parseColor(GlobalUIState.primaryColorHex)),
                            CircleShape
                        )
                        .clickable { showColorPicker = true }
                )
            }
            
            if (showColorPicker) {
                HueRingPicker(
                    initialColorHex = GlobalUIState.primaryColorHex,
                    onDismiss = { showColorPicker = false },
                    onApply = { newColor ->
                        GlobalUIState.primaryColorHex = newColor
                        runtime.androidManager.settingsService.setString("primary_color_hex", newColor)
                        showColorPicker = false
                    }
                )
            }
        }
    }
}

/**
 * MEMORY & STORAGE SETTINGS
 */
@Composable
fun MemoryStorageSettings(runtime: RemmiController) {
    val settings = runtime.androidManager.settingsService
    val authService = runtime.androidManager.googleAuthService
    val scope = rememberCoroutineScope()
    
    var currentProvider by remember {
        mutableStateOf(
            MemoryProviderType.valueOf(
                settings.getString("memory_provider_type", MemoryProviderType.DATABASE.name) ?: MemoryProviderType.DATABASE.name
            )
        )
    }
    
    var showMigrationDialog by remember { mutableStateOf<MemoryProviderType?>(null) }
    var isMigrating by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            // Success - UI will update on next composition via getLastSignedInAccount
        } catch (e: Exception) {
            Log.e("Remmi", "[GoogleSignIn] - Failed: ${e.message}")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium)) {
        RemmiCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(DesignTokens.SpacingLarge)) {
                Text(
                    "Storage Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(DesignTokens.SpacingSmall))
                
                MemoryProviderType.entries.forEach { type ->
                    val isSelected = currentProvider == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (currentProvider != type) {
                                    showMigrationDialog = type
                                }
                            }
                            .padding(vertical = DesignTokens.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = {
                            if (currentProvider != type) {
                                showMigrationDialog = type
                            }
                        })
                        Text(
                            text = when(type) {
                                MemoryProviderType.DATABASE -> "Database (Supabase)"
                                MemoryProviderType.LOCAL -> "Local Storage"
                                MemoryProviderType.GOOGLE_DRIVE -> "Google Drive"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (currentProvider == MemoryProviderType.GOOGLE_DRIVE) {
            val account = authService.getLastSignedInAccount()
            RemmiCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(DesignTokens.SpacingLarge)) {
                    Text(
                        "Google Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(DesignTokens.SpacingSmall))
                    
                    if (account != null) {
                        Text(account.email ?: "Connected", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(DesignTokens.SpacingMedium))
                        Button(
                            onClick = { authService.signOut { /* UI updates */ } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                        ) {
                            Text("Disconnect")
                        }
                    } else {
                        Text("Not connected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(DesignTokens.SpacingMedium))
                        Button(
                            onClick = { googleSignInLauncher.launch(authService.getSignInIntent()) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect Google Account")
                        }
                    }
                }
            }
        }
    }

    showMigrationDialog?.let { targetType ->
        AlertDialog(
            onDismissRequest = { showMigrationDialog = null },
            title = { Text("Change Storage System?") },
            text = { 
                Text("Your existing Remmi data is currently stored using ${currentProvider.name.lowercase().replaceFirstChar { it.uppercase() }}. " +
                     "To use ${targetType.name.lowercase().replaceFirstChar { it.uppercase() }}, your existing data can be transferred.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isMigrating = true
                            // Perform migration for known tables
                            val tables = listOf(
                                TableMigrationSpec("tasks", TaskItem.serializer()),
                                TableMigrationSpec("calendar_events", CalendarItem.serializer()),
                                TableMigrationSpec("alarms", AlarmItem.serializer())
                            )
                            try {
                                runtime.memoryMigrationService.migrate(currentProvider, targetType, tables)
                                settings.setString("memory_provider_type", targetType.name)
                                currentProvider = targetType
                            } catch (e: Exception) {
                                Log.e("Remmi", "Migration failed: ${e.message}")
                            }
                            isMigrating = false
                            showMigrationDialog = null
                        }
                    },
                    enabled = !isMigrating
                ) {
                    Text(if (isMigrating) "Transferring..." else "Transfer Data")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    settings.setString("memory_provider_type", targetType.name)
                    currentProvider = targetType
                    showMigrationDialog = null
                }) {
                    Text("Change Only")
                }
            }
        )
    }
}

/**
 * PLUGIN SETTING ITEM
 * Individual UI card for managing a single plugin's settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PluginSettingItem(
    plugin: PluginMetadata,
    onToggleEnabled: (Boolean) -> Unit,
    onToggleNavigation: (Boolean) -> Unit,
    onToggleWidget: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    Log.d("Remmi", "[SettingsScreen] - [PluginSettingItem] executed")


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (plugin.enabled) 1f else 0.5f)
            .combinedClickable(
                onClick = { /* Do nothing on click */ },
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(DesignTokens.SpacingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getIconForName(
                        plugin.icon
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(DesignTokens.IconSizeMedium),
                    tint = if (plugin.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(DesignTokens.SpacingMedium))
                Text(
                    text = plugin.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = plugin.enabled,
                    onCheckedChange = onToggleEnabled
                )
            }
            
            if (plugin.enabled) {
                HorizontalDivider(modifier = Modifier.padding(vertical = DesignTokens.SpacingSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = plugin.showInNavigation,
                                onCheckedChange = onToggleNavigation
                            )
                            Text("Show in Nav", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = plugin.showWidget,
                                onCheckedChange = onToggleWidget
                            )
                            Text("Show Widget", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
