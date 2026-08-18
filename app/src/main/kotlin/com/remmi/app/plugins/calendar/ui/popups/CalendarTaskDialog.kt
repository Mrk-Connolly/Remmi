package com.remmi.app.plugins.calendar.ui.popups

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.model.components.RepeatRule
import com.remmi.app.core.plugins.model.components.RepeatType
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTitleDescriptionGroup
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Instant?, Boolean, RepeatRule?) -> Unit
) {
    Log.d("Remmi", "[CalendarTaskDialog] - Refactored")
    var title by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDescription) }
    var isPriority by remember { mutableStateOf(false) }
    
    val timeZone = TimeZone.currentSystemDefault()
    var dueDate by remember { mutableStateOf(initialDate ?: Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone).date) }
    var isDateEnabled by remember { mutableStateOf(initialDate != null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var isRepeatable by remember { mutableStateOf(false) }
    var repeatType by remember { mutableStateOf(RepeatType.DAILY) }
    
    var addToCalendar by remember { mutableStateOf(true) }
    var addToAlarm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                RemmiTitleDescriptionGroup(
                    title = title,
                    onTitleChange = { title = it },
                    description = desc,
                    onDescriptionChange = { desc = it }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDateEnabled, onCheckedChange = { isDateEnabled = it })
                    Text("Set Due Date")
                }

                if (isDateEnabled) {
                    OutlinedTextField(
                        value = dueDate.toString(),
                        onValueChange = {},
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        readOnly = true
                    )
                }

                RemmiPrioritySwitch(
                    isPriority = isPriority,
                    onPriorityChange = { isPriority = it },
                    label = "Priority Task"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                    Text("Repeat")
                }

                if (isRepeatable) {
                    Column {
                        listOf(
                            RepeatType.DAILY,
                            RepeatType.WEEKLY,
                            RepeatType.MONTHLY,
                            RepeatType.YEARLY
                        ).forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = repeatType == type, onClick = { repeatType = type })
                                Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { addToCalendar = !addToCalendar }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Add to Calendar",
                            tint = if (addToCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(onClick = { addToAlarm = !addToAlarm }) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Add Alarm",
                            tint = if (addToAlarm) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val dueInstant = if (isDateEnabled) dueDate.atTime(0, 0).toInstant(timeZone) else null
                    val repeatRule = if (isRepeatable) RepeatRule(repeatType) else null
                    onSave(title, desc, dueInstant, isPriority, repeatRule) 
                }, 
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        RemmiDatePickerDialog(
            initialDate = dueDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { dueDate = it }
        )
    }
}
