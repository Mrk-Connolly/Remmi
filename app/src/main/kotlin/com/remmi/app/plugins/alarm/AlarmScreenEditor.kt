package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.util.UUID

sealed class AlarmEditorMode {
    init {
        Log.d("Remmi", "[AlarmEditorMode] - [constructor] executed")
    }
    data object Create : AlarmEditorMode()
    data class Edit(val alarm: AlarmItem) : AlarmEditorMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreenEditor(
    mode: AlarmEditorMode,
    actions: AlarmActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[AlarmScreenEditor] - [AlarmScreenEditor] executed")
    val scope = rememberCoroutineScope()
    val initialAlarm = (mode as? AlarmEditorMode.Edit)?.alarm

    var title by remember { mutableStateOf(initialAlarm?.title ?: "") }
    var description by remember { mutableStateOf(initialAlarm?.description ?: "") }
    var isPriority by remember { mutableStateOf(initialAlarm?.isPriority ?: false) }
    
    val timeZone = TimeZone.currentSystemDefault()
    val initialDateTime = initialAlarm?.time?.toLocalDateTime(timeZone) ?: 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)

    var hour by remember { mutableStateOf(initialDateTime.hour) }
    var minute by remember { mutableStateOf(initialDateTime.minute) }
    
    var showTimePicker by remember { mutableStateOf(false) }

    var repeatMode by remember { 
        mutableStateOf(
            when {
                initialAlarm?.repeatable?.contains("d") == true -> "Daily"
                initialAlarm?.repeatable?.contains("w") == true -> "Weekly"
                initialAlarm?.repeatable?.contains("c") == true -> "Custom"
                else -> "None"
            }
        )
    }
    val customDays = remember { 
        mutableStateListOf<String>().apply { 
            addAll(initialAlarm?.custom ?: emptyList()) 
        } 
    }
    var showDaysDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = onDismiss) { Text("Back") }
                        Button(modifier = Modifier.weight(1f), onClick = {
                            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)
                            val triggerTime = LocalDateTime(now.year, now.month, now.day, hour, minute).toInstant(timeZone)
                            
                            scope.launch {
                                val repeatable = when (repeatMode) {
                                    "Daily" -> listOf("d")
                                    "Weekly" -> listOf("w")
                                    "Custom" -> listOf("c")
                                    else -> emptyList()
                                }
                                val custom = if (repeatMode == "Custom") customDays.toList() else emptyList()

                                if (initialAlarm != null) {
                                actions.updateAlarm(
                                    initialAlarm.copy(
                                        title = title,
                                        description = description,
                                        time = triggerTime,
                                        isPriority = isPriority,
                                        repeatable = repeatable,
                                        custom = custom
                                    )
                                )
                            } else {
                                actions.addAlarm(
                                    title = title,
                                    description = description,
                                    time = triggerTime,
                                    isPriority = isPriority,
                                    repeatable = repeatable,
                                    custom = custom
                                )
                            }
                                onSave()
                            }
                        }) { Text("Save") }
                    }
                }
                Spacer(Modifier.height(96.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (initialAlarm == null) "New Alarm" else "Edit Alarm", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Priority Alarm", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isPriority, onCheckedChange = { isPriority = it })
            }

            Card(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time: ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text("Change")
                }
            }

            Text("Repeat", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("None", "Daily", "Weekly", "Custom").forEach { mode ->
                    FilterChip(
                        selected = repeatMode == mode,
                        onClick = { 
                            repeatMode = mode
                            if (mode == "Custom") showDaysDialog = true
                        },
                        label = { Text(mode) }
                    )
                }
            }

            if (repeatMode == "Custom" && customDays.isNotEmpty()) {
                Text(
                    text = "Days: ${customDays.joinToString(", ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showDaysDialog) {
        DaysSelectionDialog(
            selectedDays = customDays,
            onDismiss = { showDaysDialog = false },
            onConfirm = { days ->
                customDays.clear()
                customDays.addAll(days)
                showDaysDialog = false
            }
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            hour = timePickerState.hour
                            minute = timePickerState.minute
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
fun DaysSelectionDialog(
    selectedDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    Log.d("Remmi", "[AlarmScreenEditor] - [DaysSelectionDialog] executed")
    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedDays) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                days.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = currentSelected.contains(day),
                            onCheckedChange = { checked ->
                                if (checked) currentSelected.add(day)
                                else currentSelected.remove(day)
                            }
                        )
                        Text(day.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentSelected.toList()) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
