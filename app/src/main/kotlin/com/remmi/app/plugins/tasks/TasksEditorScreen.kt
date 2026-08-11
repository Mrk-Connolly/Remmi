package com.remmi.app.plugins.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.RepeatType
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksEditorScreen(
    mode: TaskEditorMode,
    actions: TasksActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val initialTask = (mode as? TaskEditorMode.Edit)?.task

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    
    val timeZone = TimeZone.currentSystemDefault()
    val initialDateTime = initialTask?.dueDate?.toLocalDateTime(timeZone) ?: 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)

    var day by remember { mutableStateOf(if (initialTask != null) initialDateTime.dayOfMonth.toString() else "") }
    var month by remember { mutableStateOf(if (initialTask != null) initialDateTime.monthNumber.toString() else "") }
    var year by remember { mutableStateOf(if (initialTask != null) initialDateTime.year.toString() else "") }
    
    var priority by remember { mutableStateOf(initialTask?.priority ?: Priority.Normal) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var isDueDateEnabled by remember { mutableStateOf(initialTask?.dueDate != null) }

    var isRepeatable by remember { mutableStateOf(initialTask?.repeat != null) }
    var repeatType by remember { mutableStateOf(initialTask?.repeat?.type ?: RepeatType.NONE) }
    
    var startDate by remember { mutableStateOf(initialDateTime.date) }
    var startTime by remember { mutableStateOf(initialDateTime.time) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    
    var addToCalendar by remember { mutableStateOf(initialTask?.linkedCalendar != null) }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(modifier = Modifier.weight(1f), onClick = onDismiss) { Text("Back") }
                    Button(modifier = Modifier.weight(1f), onClick = {
                        val finalDueDate = if (isDueDateEnabled) {
                            try {
                                LocalDateTime(year.toInt(), month.toInt(), day.toInt(), startTime.hour, startTime.minute).toInstant(timeZone)
                            } catch (e: Exception) {
                                initialTask?.dueDate
                            }
                        } else {
                            null
                        }
                        val repeatRule = if (isRepeatable) RepeatRule(repeatType) else null
                        
                        scope.launch {
                            if (initialTask != null) {
                                actions.updateTask(initialTask.copy(
                                    modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                                    title = title,
                                    description = description,
                                    dueDate = finalDueDate,
                                    priority = priority,
                                    repeat = repeatRule
                                ))
                            } else {
                                actions.createTask(
                                    title = title,
                                    description = description,
                                    dueDate = finalDueDate,
                                    priority = priority,
                                    repeat = repeatRule,
                                    addToCalendar = addToCalendar
                                )
                            }
                            onSave()
                        }
                    }) { Text("Save") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(if (initialTask == null) "New Task" else "Edit Task", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDueDateEnabled, onCheckedChange = { isDueDateEnabled = it })
                Text("Set Due Date")
            }

            if (isDueDateEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(2f))
                }
            }

            Text("Priority", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.name) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { isAdvancedExpanded = !isAdvancedExpanded })
            ) {
                Text("Advanced Options", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Icon(if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
            }

            if (isAdvancedExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                        Text("Repeatable")
                    }
                    if (isRepeatable) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY).forEach { type ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = repeatType == type, onClick = { repeatType = type })
                                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                    }

                    if (isDueDateEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Due Date", style = MaterialTheme.typography.labelMedium)
                                TextButton(onClick = { showStartDatePicker = true }) { Text(startDate.toString()) }
                                Text("Due Time", style = MaterialTheme.typography.labelMedium)
                                TextButton(onClick = { showStartTimePicker = true }) { Text(startTime.toString().substring(0, 5)) }
                            }
                        }
                    }
                    
                    if (initialTask == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = addToCalendar, onCheckedChange = { addToCalendar = it })
                            Text("Add to Calendar")
                        }
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val newDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                        startDate = newDate
                        day = newDate.dayOfMonth.toString()
                        month = newDate.monthNumber.toString()
                        year = newDate.year.toString()
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute)
        androidx.compose.ui.window.Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            startTime = LocalTime(timePickerState.hour, timePickerState.minute)
                            showStartTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

sealed class TaskEditorMode {
    data object Create : TaskEditorMode()
    data class Edit(val task: TaskItem) : TaskEditorMode()
}

@Composable
fun TaskDialog(
    task: TaskItem? = null,
    initialTitle: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, Instant?, Instant?, Priority, RepeatRule?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: initialTitle) }
    var desc by remember { mutableStateOf(task?.description ?: initialDescription) }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.Normal) }
    var repeatType by remember { mutableStateOf(task?.repeat?.type ?: RepeatType.NONE) }
    var addToCalendar by remember { mutableStateOf(task?.linkedCalendar != null) }
    
    var isDueDateEnabled by remember { mutableStateOf(task?.dueDate != null) }
    var startStr by remember { mutableStateOf(task?.dueDate?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "New Task" else "Edit Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDueDateEnabled, onCheckedChange = { isDueDateEnabled = it })
                    Text("Set Due Date")
                }

                if (isDueDateEnabled) {
                    OutlinedTextField(value = startStr, onValueChange = { startStr = it }, label = { Text("Due Date (ISO 8601)") }, modifier = Modifier.fillMaxWidth())
                }

                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Priority.entries.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = priority == p, onClick = { priority = p })
                            Text(p.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Text("Repeat", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(RepeatType.NONE, RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY).forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = repeatType == type, onClick = { repeatType = type })
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (task == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = addToCalendar, onCheckedChange = { addToCalendar = it })
                        Text("Add to Calendar", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val start = try { Instant.parse(startStr) } catch (e: Exception) { null }
                    val repeatRule = if (repeatType == RepeatType.NONE) null else RepeatRule(repeatType)
                    onSave(title, desc, start, null, priority, repeatRule, addToCalendar) 
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
}
