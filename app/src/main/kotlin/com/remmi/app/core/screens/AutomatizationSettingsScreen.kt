package com.remmi.app.core.screens

import android.util.Log
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.automation.DailyBriefingSettings
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.core.android.implementations.AndroidAutomationScheduler
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomatizationSettingsScreen(
    controller: RemmiController,
    onBack: () -> Unit
) {
    Log.d("Remmi", "[AutomatizationSettingsScreen] - [Content] executed")
    val repository = remember { AutomationSettingsRepository(controller.androidContext) }
    val scheduler = remember { AndroidAutomationScheduler(controller.androidContext) }
    
    var settings by remember { mutableStateOf(repository.getBriefingSettings()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(settings.enabled) {
        if (settings.enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Daily Briefing Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Briefing",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Receive a summary of your day every morning.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        HorizontalDivider()
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Briefing Time",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Scheduled for ${settings.hour.toString().padStart(2, '0')}:${settings.minute.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Button(onClick = { showTimePicker = true }) {
                                Text("Change")
                            }
                        }
                    }
                }
            }

            // Info Card
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "The briefing includes today's calendar events, pending tasks, weather forecast, and recommendations.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
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
