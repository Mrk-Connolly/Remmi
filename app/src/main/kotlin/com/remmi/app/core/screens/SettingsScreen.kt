package com.remmi.app.core.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.PluginMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasChanges) {
                ExtendedFloatingActionButton(
                    onClick = {
                        pluginManager.updateAllPluginSettings(runtime.androidManager.fileService, pendingMetadata)
                        pluginManager.loadPlugins()
                        onBack()
                    },
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    text = { Text("Apply Changes") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                )
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
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Theme", style = MaterialTheme.typography.titleSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    com.remmi.app.core.controller.RemmiThemeMode.LIGHT to "Light",
                                    com.remmi.app.core.controller.RemmiThemeMode.DARK to "Dark",
                                    com.remmi.app.core.controller.RemmiThemeMode.SYSTEM to "System"
                                ).forEach { (mode, label) ->
                                    FilterChip(
                                        selected = GlobalUIState.themePreference.value == mode,
                                        onClick = { 
                                            GlobalUIState.themePreference.value = mode
                                            runtime.androidManager.settingsService.setString("theme_pref", mode.name)
                                        },
                                        label = { Text(label) },
                                        shape = CircleShape
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("Primary Color", style = MaterialTheme.typography.titleSmall)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                com.remmi.app.core.ui.PrimaryPalette.forEach { colorHex ->
                                    val color = Color(android.graphics.Color.parseColor(colorHex))
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(color, CircleShape)
                                            .clickable { 
                                                GlobalUIState.primaryColorHex.value = colorHex
                                                runtime.androidManager.settingsService.setString("primary_color_hex", colorHex)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        if (GlobalUIState.primaryColorHex.value == colorHex) {
                                            Icon(
                                                Icons.Default.Check, 
                                                contentDescription = null, 
                                                tint = Color.White,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "System Features",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(RemmiDestination.AUTOMATIZATION_ROUTE) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(text = "Daily Briefing & Automations", modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }

                item {
                    Text(
                        text = "Plugin Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (plugin.enabled) 1f else 0.5f)
            .combinedClickable(
                onClick = { /* Do nothing on click */ },
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getIconForName(plugin.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (plugin.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.width(12.dp))
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
