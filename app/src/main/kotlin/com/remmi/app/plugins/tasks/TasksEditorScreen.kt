package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.RepeatType
import com.remmi.app.plugins.calendar.RepeatDaysDialog
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch

sealed class TaskEditorMode {
    init {
        Log.d("Remmi", "[TaskEditorMode] - [constructor] executed")
    }
    data object Create : TaskEditorMode()
    data class Edit(val task: TaskItem) : TaskEditorMode()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksEditorScreen(
    mode: TaskEditorMode,
    actions: TasksActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[TasksEditorScreen] - [TasksEditorScreen] executed")
    val scope = rememberCoroutineScope()
    val initialTask = (mode as? TaskEditorMode.Edit)?.task

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    
    val timeZone = TimeZone.currentSystemDefault()
    val initialDateTime = initialTask?.dueDate?.toLocalDateTime(timeZone) ?: 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)

    var startDate by remember { mutableStateOf(initialDateTime.date) }
    var startTime by remember { mutableStateOf(initialDateTime.time) }
    
    var isPriority by remember { mutableStateOf(initialTask?.isPriority ?: false) }
    var group by remember { mutableStateOf(initialTask?.group) }
    
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var isDueDateEnabled by remember { mutableStateOf(initialTask?.dueDate != null) }
    var isTimeEnabled by remember { mutableStateOf(initialTask?.dueDate != null) }

    var repeatType by remember { mutableStateOf(initialTask?.repeat?.type ?: RepeatType.NONE) }
    var repeatDays by remember { mutableStateOf(initialTask?.repeat?.days ?: emptyList()) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }
    var showAlarmTimePicker by remember { mutableStateOf(false) }

    var existingGroups by remember { mutableStateOf(emptyList<String>()) }
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
    }
    
    var addToCalendar by remember { mutableStateOf(initialTask?.linkedCalendar != null) }
    var addToAlarm by remember { mutableStateOf(false) }
    var alarmTime by remember { mutableStateOf<LocalTime?>(null) }

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
                            val finalDueDate = if (isDueDateEnabled) {
                                try {
                                    val timeToUse = if (isTimeEnabled) startTime else LocalTime(23, 59)
                                    LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, timeToUse.hour, timeToUse.minute).toInstant(timeZone)
                                } catch (e: Exception) {
                                    initialTask?.dueDate
                                }
                            } else {
                                null
                            }
                            val repeatRule = if (repeatType != RepeatType.NONE) {
                                RepeatRule(repeatType, if (repeatType == RepeatType.CUSTOM) repeatDays else emptyList())
                            } else {
                                null
                            }

                            val finalAlarmInstant = if (addToAlarm && alarmTime != null) {
                                try {
                                    LocalDateTime(startDate.year, startDate.monthNumber, startDate.dayOfMonth, alarmTime!!.hour, alarmTime!!.minute).toInstant(timeZone)
                                } catch (e: Exception) {
                                    null
                                }
                            } else {
                                null
                            }
                            
                            scope.launch {
                                if (initialTask != null) {
                                    actions.updateTask(initialTask.copy(
                                        modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                                        title = title,
                                        description = description,
                                        dueDate = finalDueDate,
                                        isPriority = isPriority,
                                        group = group,
                                        repeat = repeatRule
                                    ))
                                } else {
                                    actions.createTask(
                                        title = title,
                                        description = description,
                                        dueDate = finalDueDate,
                                        isPriority = isPriority,
                                        group = group,
                                        repeat = repeatRule,
                                        addToCalendar = addToCalendar,
                                        addToAlarm = addToAlarm,
                                        alarmTime = finalAlarmInstant
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
            Text(if (initialTask == null) "New Task" else "Edit Task", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(
                    expanded = isGroupExpanded,
                    onExpandedChange = { isGroupExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = group ?: "No Group",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isGroupExpanded,
                        onDismissRequest = { isGroupExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Group") },
                            onClick = { group = null; isGroupExpanded = false }
                        )
                        existingGroups.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = { group = g; isGroupExpanded = false }
                            )
                        }
                    }
                }
                IconButton(onClick = { showAddGroupDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New Group")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Priority Task", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isPriority, onCheckedChange = { isPriority = it })
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { isAdvancedExpanded = !isAdvancedExpanded }
            ) {
                Text("Advanced Options", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Icon(if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
            }

            if (isAdvancedExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // --- Repeatable Section ---
                    Column {
                        Text("Repeat", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Using a simple Row for now, as I don't want to risk FlowRow being missing
                            Column {
                                Row {
                                    listOf(RepeatType.NONE, RepeatType.DAILY, RepeatType.WEEKLY).forEach { type ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                            RadioButton(
                                                selected = repeatType == type,
                                                onClick = { 
                                                    repeatType = type
                                                }
                                            )
                                            Text(
                                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Row {
                                    listOf(RepeatType.MONTHLY, RepeatType.YEARLY, RepeatType.CUSTOM).forEach { type ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                            RadioButton(
                                                selected = repeatType == type,
                                                onClick = { 
                                                    repeatType = type
                                                    if (type == RepeatType.CUSTOM) showRepeatDaysDialog = true
                                                }
                                            )
                                            Text(
                                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (repeatType == RepeatType.CUSTOM) {
                            Text(
                                text = "Selected: ${repeatDays.joinToString { it.name.lowercase().take(3).replaceFirstChar { it.uppercase() } }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { showRepeatDaysDialog = true }.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // --- Due Date Section ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Due Date & Time", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = if (isDueDateEnabled) startDate.toString() else "Not set",
                            onValueChange = {},
                            label = { Text("Due Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDueDateEnabled = true; showStartDatePicker = true },
                            readOnly = true,
                            leadingIcon = {
                                Checkbox(checked = isDueDateEnabled, onCheckedChange = { isDueDateEnabled = it })
                            },
                            trailingIcon = {
                                if (isDueDateEnabled) {
                                    IconButton(onClick = { showStartDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                                    }
                                }
                            }
                        )

                        if (isDueDateEnabled) {
                            OutlinedTextField(
                                value = if (isTimeEnabled) startTime.toString().substring(0, 5) else "Default (23:59)",
                                onValueChange = {},
                                label = { Text("Due Time") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isTimeEnabled = true; showStartTimePicker = true },
                                readOnly = true,
                                leadingIcon = {
                                    Checkbox(checked = isTimeEnabled, onCheckedChange = { isTimeEnabled = it })
                                },
                                trailingIcon = {
                                    if (isTimeEnabled) {
                                        IconButton(onClick = { showStartTimePicker = true }) {
                                            Icon(Icons.Default.AccessTime, contentDescription = "Set Time")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // --- Action Options ---
                    if (initialTask == null) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Quick Actions", style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = addToCalendar, onCheckedChange = { addToCalendar = it })
                                Text("Create Calendar Event")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = addToAlarm, onCheckedChange = { 
                                    addToAlarm = it
                                    if (it) showAlarmTimePicker = true
                                })
                                Text("Create Alarm")
                                if (addToAlarm && alarmTime != null) {
                                    Text(
                                        " at ${alarmTime.toString().substring(0, 5)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showAlarmTimePicker = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Add New Group") },
            text = {
                OutlinedTextField(value = newGroupName, onValueChange = { newGroupName = it }, label = { Text("Group Name") })
            },
            confirmButton = {
                Button(onClick = {
                    if (newGroupName.isNotBlank()) {
                        group = newGroupName
                        showAddGroupDialog = false
                        newGroupName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) { Text("Cancel") }
            }
        )
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
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showRepeatDaysDialog) {
        RepeatDaysDialog(
            selectedDays = repeatDays.map { it.name },
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { days ->
                repeatDays = days.map { DayOfWeek.valueOf(it) }
                showRepeatDaysDialog = false
            }
        )
    }

    if (showAlarmTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = alarmTime?.hour ?: 8, initialMinute = alarmTime?.minute ?: 0)
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAlarmTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Alarm Time", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { 
                            showAlarmTimePicker = false
                            if (alarmTime == null) addToAlarm = false 
                        }) { Text("Cancel") }
                        TextButton(onClick = {
                            alarmTime = LocalTime(timePickerState.hour, timePickerState.minute)
                            showAlarmTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
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

@Composable
fun TaskDialog(
    task: TaskItem? = null,
    initialTitle: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String, Instant?, Instant?, Boolean, RepeatRule?, Boolean) -> Unit
) {
    Log.d("Remmi", "[TasksEditorScreen] - [TaskDialog] executed")
    var title by remember { mutableStateOf(task?.title ?: initialTitle) }
    var desc by remember { mutableStateOf(task?.description ?: initialDescription) }
    var repeatType by remember { mutableStateOf(task?.repeat?.type ?: RepeatType.NONE) }
    var isPriority by remember { mutableStateOf(task?.isPriority ?: false) }
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Priority Task", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = isPriority, onCheckedChange = { isPriority = it })
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
                    onSave(title, desc, start, null, isPriority, repeatRule, addToCalendar) 
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
