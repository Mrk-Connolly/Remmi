package com.remmi.app.plugins.calendar.ui.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.core.screens.components.RemmiTitleDescriptionGroup
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
    Log.d("Remmi", "[AlarmDialog] - Refactored")
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
                RemmiTitleDescriptionGroup(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )
                
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

                RemmiPrioritySwitch(
                    isPriority = isPriority,
                    onPriorityChange = { isPriority = it },
                    label = "Priority Alarm"
                )
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
