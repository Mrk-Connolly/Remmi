package com.remmi.app.ui.screens.launcher.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.android.launcher.LauncherManagerService
import com.remmi.app.ui.screens.launcher.data.LauncherPreferencesRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefsRepo = remember { LauncherPreferencesRepository(context) }
    val launcherManager = remember { LauncherManagerService(context) }

    var isDefaultLauncher by remember { mutableStateOf(launcherManager.isDefaultLauncher()) }
    var moduleConfigs by remember { mutableStateOf(prefsRepo.getModuleConfigs()) }

    var swipeDownNotifications by remember { mutableStateOf(prefsRepo.isSwipeDownNotifications) }
    var showClockHeader by remember { mutableStateOf(prefsRepo.showClockHeader) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Launcher Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section 1: Default Launcher Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefaultLauncher) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = "Default Home")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDefaultLauncher) "Remmi is Default Launcher" else "Remmi is Not Default Launcher",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isDefaultLauncher)
                                "Pressing the Home button opens Remmi Dashboard."
                            else
                                "Set Remmi as your default Home app to replace your standard home screen.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                launcherManager.openHomeSettings()
                                isDefaultLauncher = launcherManager.isDefaultLauncher()
                            }
                        ) {
                            Text(if (isDefaultLauncher) "Change Default Launcher" else "Set as Default Launcher")
                        }
                    }
                }
            }

            // Section 2: Dashboard Modules & Ordering
            item {
                Text(
                    text = "DASHBOARD MODULES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            itemsIndexed(moduleConfigs) { index, config ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Switch(
                                checked = config.enabled,
                                onCheckedChange = { checked ->
                                    val updated = moduleConfigs.toMutableList()
                                    updated[index] = config.copy(enabled = checked)
                                    moduleConfigs = updated
                                    prefsRepo.saveModuleConfigs(updated)
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = config.type.defaultTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Reorder buttons
                        Row {
                            IconButton(
                                enabled = index > 0,
                                onClick = {
                                    val updated = moduleConfigs.toMutableList()
                                    val item = updated.removeAt(index)
                                    updated.add(index - 1, item)
                                    val reindexed = updated.mapIndexed { idx, cfg -> cfg.copy(priority = idx) }
                                    moduleConfigs = reindexed.toMutableList()
                                    prefsRepo.saveModuleConfigs(reindexed)
                                }
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                            }
                            IconButton(
                                enabled = index < moduleConfigs.size - 1,
                                onClick = {
                                    val updated = moduleConfigs.toMutableList()
                                    val item = updated.removeAt(index)
                                    updated.add(index + 1, item)
                                    val reindexed = updated.mapIndexed { idx, cfg -> cfg.copy(priority = idx) }
                                    moduleConfigs = reindexed.toMutableList()
                                    prefsRepo.saveModuleConfigs(reindexed)
                                }
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                            }
                        }
                    }
                }
            }

            // Section 3: Gestures & Display
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GESTURES & DISPLAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Swipe Down for Notifications",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Expand notification shade on swipe down gesture",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = swipeDownNotifications,
                                onCheckedChange = { checked ->
                                    swipeDownNotifications = checked
                                    prefsRepo.isSwipeDownNotifications = checked
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Live Clock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Display prominent date and time on dashboard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = showClockHeader,
                                onCheckedChange = { checked ->
                                    showClockHeader = checked
                                    prefsRepo.showClockHeader = checked
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
