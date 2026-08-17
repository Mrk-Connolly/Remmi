package com.remmi.app.core.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.navigation.getIconForName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SETTINGS SCREEN
 * Configuration page for plugin management and system settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    runtime: RemmiController,
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
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // In a real app, this might reload settings from disk or server
            delay(500)
            isRefreshing = false
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
                        pluginManager.updateAllPluginSettings(runtime.serviceManager.fileService, pendingMetadata)
                        pluginManager.loadPlugins()
                        onBack()
                    },
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    text = { Text("Apply Changes") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        bottomBar = {
            Spacer(Modifier.height(96.dp))
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Plugin Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(pendingMetadata) { plugin ->
                    PluginSettingItem(
                        plugin = plugin,
                        onToggleEnabled = { enabled ->
                            pendingMetadata = pendingMetadata.map { 
                                if (it.id == plugin.id) it.copy(enabled = enabled) else it
                            }
                        },
                        onToggleNavigation = { show ->
                            pendingMetadata = pendingMetadata.map { 
                                if (it.id == plugin.id) it.copy(showInNavigation = show) else it
                            }
                        },
                        onToggleWidget = { show ->
                            pendingMetadata = pendingMetadata.map { 
                                if (it.id == plugin.id) it.copy(showWidget = show) else it
                            }
                        },
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
