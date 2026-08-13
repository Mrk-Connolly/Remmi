package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDialog(
    initialTitle: String,
    initialDescription: String,
    initialDate: LocalDate,
    initialIsPriority: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, LocalTime, Boolean) -> Unit
) {
    Log.d("Remmi", "[AlarmDialog] - [AlarmDialog] executed")
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var isPriority by remember { mutableStateOf(initialIsPriority) }
    
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Alarm to Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                Text("Date: $initialDate", style = MaterialTheme.typography.bodyMedium)
                
                Card(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alarm Time: ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Set Time")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Priority Alarm", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isPriority, onCheckedChange = { isPriority = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(title, description, LocalTime(hour, minute), isPriority)
            }) {
                Text("Add Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

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
