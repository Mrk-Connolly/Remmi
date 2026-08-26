package com.remmi.app.plugins.alarm.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.commands.CreateAlarmCommand
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
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
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(timeZone).date }
    
    var alarmDate by remember { mutableStateOf(today) }
    var alarmTime by remember { mutableStateOf(LocalTime(9, 0)) }
    
    var day by remember { mutableStateOf(today.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(today.monthNumber.toString()) }
    var year by remember { mutableStateOf(today.year.toString()) }

    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Linked Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RemmiTitleDescriptionGroup(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )
                
                // Day, Month, Year section
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(1.5f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }

                // Time section
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = alarmTime.toString().substring(0, 5),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Select Time")
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
                val finalDate = try { LocalDate(year.toInt(), month.toInt(), day.toInt()) } catch (e: Exception) { alarmDate }
                val finalTime = finalDate.atTime(alarmTime).toInstant(timeZone)
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

    if (showDatePicker) {
        RemmiDatePickerDialog(
            initialDate = alarmDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { 
                alarmDate = it
                day = it.dayOfMonth.toString()
                month = it.monthNumber.toString()
                year = it.year.toString()
            }
        )
    }
}
