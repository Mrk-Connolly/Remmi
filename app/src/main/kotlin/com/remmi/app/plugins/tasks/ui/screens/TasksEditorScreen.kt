package com.remmi.app.plugins.tasks.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugins.model.components.RepeatRule
import com.remmi.app.core.plugins.model.components.RepeatType
import com.remmi.app.core.screens.components.*
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.TaskItem
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch

sealed class TaskEditorMode {
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
    Log.d("Remmi", "[TasksEditorScreen] - Refactored")
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
    var alarmDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAlarmDatePicker by remember { mutableStateOf(false) }

    RemmiEditorScaffold(
        title = if (initialTask == null) "New Task" else "Edit Task",
        onBack = onDismiss,
        onSave = {
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
                        repeat = repeatRule
                        // Note: addToCalendar and addToAlarm are handled by future automation facts
                    )
                }
                onSave()
            }
        },
        saveEnabled = title.isNotBlank()
    ) {
        RemmiTitleDescriptionGroup(
            title = title,
            onTitleChange = { title = it },
            description = description,
            onDescriptionChange = { description = it }
        )

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

        RemmiPrioritySwitch(
            isPriority = isPriority,
            onPriorityChange = { isPriority = it },
            label = "Priority Task"
        )

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { addToCalendar = !addToCalendar },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = if (addToCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Create Calendar Event")
                            }
                            
                            Spacer(Modifier.width(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { 
                                        addToAlarm = !addToAlarm
                                        if (addToAlarm) {
                                            if (isDueDateEnabled) alarmDate = startDate
                                            showAlarmTimePicker = true
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (addToAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Icon(Icons.Default.Alarm, contentDescription = "Create Alarm")
                                }
                                if (addToAlarm) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isDueDateEnabled && alarmDate != null) {
                                            Text(
                                                text = alarmDate.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable { showAlarmDatePicker = true }
                                            )
                                            Text(" at ", style = MaterialTheme.typography.labelMedium)
                                        }
                                        if (alarmTime != null) {
                                            Text(
                                                text = alarmTime.toString().substring(0, 5),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable { showAlarmTimePicker = true }
                                            )
                                        } else {
                                            Text(
                                                text = "Set Time",
                                                style = MaterialTheme.typography.labelMedium,
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
        RemmiDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { newDate ->
                startDate = newDate
            }
        )
    }

    if (showRepeatDaysDialog) {
        RemmiDaySelectionDialog(
            selectedDays = repeatDays.map { it.name },
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { days ->
                repeatDays = days.map { DayOfWeek.valueOf(it) }
                showRepeatDaysDialog = false
            }
        )
    }

    if (showAlarmDatePicker) {
        RemmiDatePickerDialog(
            initialDate = alarmDate ?: startDate,
            onDismiss = { showAlarmDatePicker = false },
            onDateSelected = { newDate ->
                alarmDate = newDate
            }
        )
    }

    if (showAlarmTimePicker) {
        RemmiTimePickerDialog(
            initialTime = alarmTime ?: LocalTime(8, 0),
            onDismiss = { showAlarmTimePicker = false },
            onTimeSelected = { time ->
                alarmTime = time
            }
        )
    }

    if (showStartTimePicker) {
        RemmiTimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { time ->
                startTime = time
            }
        )
    }
}
