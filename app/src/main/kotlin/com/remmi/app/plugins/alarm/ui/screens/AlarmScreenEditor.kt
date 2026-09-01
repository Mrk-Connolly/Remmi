package com.remmi.app.plugins.alarm.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.commands.CreateAlarmCommand
import com.remmi.app.core.eventBus.commands.UpdateAlarmCommand
import com.remmi.app.core.eventBus.commands.DeleteAlarmCommand
import com.remmi.app.ui.components.RemmiAddScreen
import com.remmi.app.ui.components.RemmiModifyScreen
import com.remmi.app.ui.components.RemmiPrioritySwitch
import com.remmi.app.ui.components.RemmiTitleDescriptionGroup
import com.remmi.app.ui.popups.RemmiTimePickerDialog
import com.remmi.app.ui.popups.RemmiDaySelectionDialog
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.alarm.models.AlarmItem
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

sealed class AlarmEditorMode {
    data object Create : AlarmEditorMode()
    data class Edit(val alarm: AlarmItem) : AlarmEditorMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreenEditor(
    mode: AlarmEditorMode,
    actions: AlarmActions,
    controller: RemmiController,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[AlarmScreenEditor] - Refactored")
    val scope = rememberCoroutineScope()
    val initialAlarm = (mode as? AlarmEditorMode.Edit)?.alarm

    var title by remember { mutableStateOf(initialAlarm?.title ?: "") }
    var description by remember { mutableStateOf(initialAlarm?.description ?: "") }
    var isPriority by remember { mutableStateOf(initialAlarm?.isPriority ?: false) }
    var useSound by remember { mutableStateOf(initialAlarm?.useSound ?: true) }
    var useVibration by remember { mutableStateOf(initialAlarm?.useVibration ?: true) }
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val initialDateTime = remember(initialAlarm) {
        initialAlarm?.time?.toLocalDateTime(timeZone) ?: 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)
    }

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

    val onSaveAction = {
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
                controller.eventBus.publishCommand(
                    UpdateAlarmCommand(
                        alarm = initialAlarm.copy(
                            title = title,
                            description = description,
                            time = triggerTime,
                            isPriority = isPriority,
                            repeatable = repeatable,
                            custom = custom,
                            useSound = useSound,
                            useVibration = useVibration
                        )
                    )
                )
            } else {
                controller.eventBus.publishCommand(
                    CreateAlarmCommand(
                        title = title,
                        description = description,
                        time = triggerTime,
                        isPriority = isPriority,
                        repeatable = repeatable,
                        custom = custom,
                        syncToSystem = true
                    )
                )
            }
            onSave()
        }
    }

    if (initialAlarm == null) {
        RemmiAddScreen(
            title = "New Alarm",
            onBack = onDismiss,
            onSave = { onSaveAction() },
            saveEnabled = title.isNotBlank()
        ) {
            EditorContent(
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                useSound = useSound, onUseSoundChange = { useSound = it },
                useVibration = useVibration, onUseVibrationChange = { useVibration = it },
                hour = hour, minute = minute,
                onShowTimePicker = { showTimePicker = true },
                repeatMode = repeatMode, onRepeatModeChange = { repeatMode = it },
                customDays = customDays,
                onShowDaysDialog = { showDaysDialog = true }
            )
        }
    } else {
        RemmiModifyScreen(
            title = "Edit Alarm",
            onBack = onDismiss,
            onDelete = {
                scope.launch {
                    controller.eventBus.publishCommand(DeleteAlarmCommand(alarmId = initialAlarm.id))
                    onSave()
                }
            },
            onSave = { onSaveAction() },
            saveEnabled = title.isNotBlank()
        ) {
            EditorContent(
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                useSound = useSound, onUseSoundChange = { useSound = it },
                useVibration = useVibration, onUseVibrationChange = { useVibration = it },
                hour = hour, minute = minute,
                onShowTimePicker = { showTimePicker = true },
                repeatMode = repeatMode, onRepeatModeChange = { repeatMode = it },
                customDays = customDays,
                onShowDaysDialog = { showDaysDialog = true }
            )
        }
    }

    if (showDaysDialog) {
        RemmiDaySelectionDialog(
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
        RemmiTimePickerDialog(
            initialTime = LocalTime(hour, minute),
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                hour = time.hour
                minute = time.minute
            }
        )
    }
}

@Composable
private fun EditorContent(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    isPriority: Boolean, onIsPriorityChange: (Boolean) -> Unit,
    useSound: Boolean, onUseSoundChange: (Boolean) -> Unit,
    useVibration: Boolean, onUseVibrationChange: (Boolean) -> Unit,
    hour: Int, minute: Int,
    onShowTimePicker: () -> Unit,
    repeatMode: String, onRepeatModeChange: (String) -> Unit,
    customDays: List<String>,
    onShowDaysDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RemmiTitleDescriptionGroup(
            title = title,
            onTitleChange = onTitleChange,
            description = description,
            onDescriptionChange = onDescriptionChange
        )

        RemmiPrioritySwitch(
            isPriority = isPriority,
            onPriorityChange = onIsPriorityChange,
            label = "Priority Alarm"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = useSound, onCheckedChange = onUseSoundChange)
                Text("Sound", style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = useVibration, onCheckedChange = onUseVibrationChange)
                Text("Vibration", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(
            onClick = onShowTimePicker,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("None", "Daily", "Weekly", "Custom").forEach { mode ->
                FilterChip(
                    selected = repeatMode == mode,
                    onClick = { 
                        onRepeatModeChange(mode)
                        if (mode == "Custom") onShowDaysDialog()
                    },
                    label = { Text(mode, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                    modifier = Modifier.weight(1f)
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
