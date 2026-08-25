package com.remmi.app.plugins.alarm.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.commands.CreateAlarmCommand
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.core.screens.components.RemmiTitleDescriptionGroup
import com.remmi.app.core.controller.LinkedCreationData
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfigurationDialog(
    data: LinkedCreationData,
    onDismiss: () -> Unit,
    onConfirm: (CreateAlarmCommand) -> Unit
) {
    Log.d("Remmi", "[AlarmConfigurationDialog] - Displaying for ${data.sourcePlugin}")
    var title by remember { mutableStateOf(data.title) }
    var description by remember { mutableStateOf(data.description) }
    var isPriority by remember { mutableStateOf(false) }
    
    val timeZone = TimeZone.currentSystemDefault()
    val today = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(timeZone).date
    
    var alarmDate by remember { mutableStateOf(today) }
    var alarmTime by remember { mutableStateOf(LocalTime(9, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Linked Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RemmiTitleDescriptionGroup(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )
                
                Card(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time: ${alarmTime.toString().substring(0, 5)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Set Time")
                    }
                }

                RemmiPrioritySwitch(
                    isPriority = isPriority,
                    onPriorityChange = { isPriority = it },
                    label = "Priority Alarm"
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val finalTime = alarmDate.atTime(alarmTime).toInstant(timeZone)
                onConfirm(
                    CreateAlarmCommand(
                        title = title,
                        description = description,
                        time = finalTime,
                        isPriority = isPriority,
                        sourcePlugin = data.sourcePlugin,
                        sourceItemId = data.sourceItemId,
                        correlationId = data.correlationId,
                        causationId = data.causationId,
                        creationContext = CreationContext.SECONDARY_LINKED,
                        source = "alarm_plugin_ui"
                    )
                )
            }) {
                Text("Create Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showTimePicker) {
        RemmiTimePickerDialog(
            initialTime = alarmTime,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { alarmTime = it }
        )
    }
}
