package com.remmi.app.core.screens

import android.util.Log
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.automation.features.dailybriefing.DailyBriefingSettings
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.core.android.system.AndroidAutomationScheduler
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomatizationSettingsScreen(
    controller: RemmiController,
    onBack: () -> Unit
) {
    Log.d("Remmi", "[AutomatizationSettingsScreen] - [Content] executed")
    val repository = remember { controller.automationEngine.settingsRepository }
    val scheduler = remember { AndroidAutomationScheduler(controller.androidContext) }
    val scope = rememberCoroutineScope()
    
    var settings by remember { mutableStateOf(repository.getBriefingSettings()) }
    var lockScreenEnabled by remember { mutableStateOf(repository.isLockScreenSummaryEnabled()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(settings.enabled, lockScreenEnabled) {
        if ((settings.enabled || lockScreenEnabled) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Automatization Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Daily Briefing Section
            com.remmi.app.core.ui.RemmiCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Briefing",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Receive a summary of your day every morning.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = settings.enabled,
                            onCheckedChange = { enabled ->
                                settings = settings.copy(enabled = enabled)
                                saveAndSchedule(repository, scheduler, settings)
                            }
                        )
                    }

                    if (settings.enabled) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Briefing Time",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Scheduled for ${settings.hour.toString().padStart(2, '0')}:${settings.minute.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(
                                    onClick = { showTimePicker = true },
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }
            }

            // Lock Screen Summary Section
            com.remmi.app.core.ui.RemmiCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lock Screen Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "Show your weekly schedule and tasks on the lock screen.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = lockScreenEnabled,
                        onCheckedChange = { enabled ->
                            lockScreenEnabled = enabled
                            repository.setLockScreenSummaryEnabled(enabled)
                            // Trigger immediate refresh to show/hide
                            scope.launch {
                                controller.automationEngine.lockScreenManager.refreshSummary()
                            }
                        }
                    )
                }
            }

            // Info Card
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp)) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "The briefing includes today's calendar events, pending tasks, weather forecast, and recommendations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        RemmiTimePickerDialog(
            initialTime = LocalTime(settings.hour, settings.minute),
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                settings = settings.copy(hour = time.hour, minute = time.minute)
                saveAndSchedule(repository, scheduler, settings)
                showTimePicker = false
            }
        )
    }
}

private fun saveAndSchedule(
    repository: AutomationSettingsRepository,
    scheduler: AndroidAutomationScheduler,
    settings: DailyBriefingSettings
) {
    repository.updateBriefingSettings(settings)
    if (settings.enabled) {
        scheduler.scheduleDailyBriefing(settings)
    } else {
        scheduler.cancelDailyBriefing()
    }
}
