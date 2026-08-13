package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID

sealed class EditorMode {
    init {
        Log.d("Remmi", "[EditorMode] - [constructor] executed")
    }
    data class Create(val initialDate: LocalDate? = null) : EditorMode()
    data class Edit(val event: CalendarItem) : EditorMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEditorScreen(
    mode: EditorMode,
    actions: CalendarActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[CalendarScreenEditor] - [CalendarEditorScreen] executed")
    val scope = rememberCoroutineScope()
    val initialEvent = (mode as? EditorMode.Edit)?.event

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    
    val timeZone = TimeZone.currentSystemDefault()
    val initialDate = initialEvent?.startingDate ?: (mode as? EditorMode.Create)?.initialDate ?: Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone).date
    val initialTime = initialEvent?.startingTime ?: LocalTime(0, 0)

    var day by remember { mutableStateOf(initialDate.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(initialDate.monthNumber.toString()) }
    var year by remember { mutableStateOf(initialDate.year.toString()) }
    
    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    var group by remember { mutableStateOf(initialEvent?.group) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var existingGroups by remember { mutableStateOf(emptyList<String>()) }
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
    }

    var isRepeatable by remember { mutableStateOf(initialEvent?.repeat?.isNotEmpty() == true) }
    var repeatList by remember { mutableStateOf(initialEvent?.repeat ?: emptyList<String>()) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialTime) }
    var endDate by remember { mutableStateOf(initialEvent?.endingDate ?: initialDate) }
    var endTime by remember { 
        mutableStateOf(initialEvent?.endingTime ?: LocalTime(23, 59)) 
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var showTaskDialog by remember { mutableStateOf(false) }
    val linkedTaskIds = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.linkedTasks ?: emptyList()) } }
    val currentEventId = remember { initialEvent?.id ?: UUID.randomUUID().toString() }
    
    var showLocationDialog by remember { mutableStateOf(false) }
    val locations = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.location ?: emptyList()) } }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    val participants = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.participants ?: emptyList()) } }
    
    var showAlarmConfirmation by remember { mutableStateOf(false) }

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
                            val finalStartDate = try { LocalDate(year.toInt(), month.toInt(), day.toInt()) } catch (e: Exception) { startDate }
                            
                            scope.launch {
                                if (initialEvent != null) {
                                    actions.updateEvent(initialEvent.copy(
                                        modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                                        title = title,
                                        description = description,
                                        startingDate = finalStartDate,
                                        startingTime = startTime,
                                        endingDate = endDate,
                                        endingTime = endTime,
                                        isPriority = isPriority,
                                        group = group,
                                        repeat = repeatList,
                                        linkedTasks = linkedTaskIds,
                                        location = locations,
                                        participants = participants
                                    ))
                                } else {
                                    actions.addEvent(
                                        id = currentEventId,
                                        title = title,
                                        description = description,
                                        startingDate = finalStartDate,
                                        startingTime = startTime,
                                        endingDate = endDate,
                                        endingTime = endTime,
                                        isPriority = isPriority,
                                        group = group,
                                        repeat = repeatList,
                                        linkedTasks = linkedTaskIds,
                                        location = locations,
                                        participants = participants
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
            Text(if (initialEvent == null) "New Event" else "Edit Event", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(2f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Priority Event", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isPriority, onCheckedChange = { isPriority = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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

            Text("Quick Add", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = { showTaskDialog = true }) { Icon(Icons.Default.CheckCircle, "Task") }
                IconButton(onClick = { showAlarmConfirmation = true }) { Icon(Icons.Default.Alarm, "Alarm") }
                IconButton(onClick = { showParticipantsDialog = true }) { Icon(Icons.Default.Person, "Participants") }
                IconButton(onClick = { showLocationDialog = true }) { Icon(Icons.Default.LocationOn, "Location") }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                        Text("Repeatable")
                    }
                    if (isRepeatable) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { type ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = repeatList.contains(type), onClick = { repeatList = listOf(type) })
                                        Text(
                                            text = type.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = repeatList.size > 1 || (repeatList.isNotEmpty() && !listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").contains(repeatList[0])), onClick = { 
                                    showRepeatDaysDialog = true
                                })
                                Text("Optional")
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showStartDatePicker = true }) { Text(startDate.toString()) }
                            Text("Start Time", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showStartTimePicker = true }) { Text(startTime.toString().substring(0, 5)) }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showEndDatePicker = true }) { Text(endDate.toString()) }
                            Text("End Time", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showEndTimePicker = true }) { Text(endTime.toString().substring(0, 5)) }
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

    if (showRepeatDaysDialog) {
        RepeatDaysDialog(
            selectedDays = repeatList,
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { 
                repeatList = it
                showRepeatDaysDialog = false
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
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
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

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = endTime.hour, initialMinute = endTime.minute)
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            endTime = LocalTime(timePickerState.hour, timePickerState.minute)
                            showEndTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            initialTitle = title,
            initialDescription = description,
            initialDate = startDate,
            onDismiss = { showTaskDialog = false },
            onSave = { t, d, s, p, r ->
                scope.launch {
                    val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                    val newTaskId = UUID.randomUUID().toString()
                    val newTask = com.remmi.app.plugins.tasks.TaskItem(
                        id = newTaskId,
                        created = now,
                        modified = now,
                        title = t,
                        description = d,
                        dueDate = s,
                        isPriority = p,
                        group = group,
                        completed = false,
                        repeat = r,
                        linkedCalendar = currentEventId
                    )
                    actions.addTask(newTask)
                    linkedTaskIds.add(newTaskId)
                    showTaskDialog = false
                }
            }
        )
    }

    if (showAlarmConfirmation) {
        AlarmDialog(
            initialTitle = title,
            initialDescription = description,
            initialDate = startDate,
            initialIsPriority = isPriority,
            onDismiss = { showAlarmConfirmation = false },
            onSave = { t, d, time, p ->
                scope.launch {
                    val alarmActions = actions.getAlarmActions()
                    if (alarmActions != null) {
                        val alarmTime = startDate.atTime(time).toInstant(timeZone)
                        alarmActions.addAlarm(
                            title = t,
                            description = d,
                            time = alarmTime,
                            isPriority = p
                        )
                    }
                    showAlarmConfirmation = false
                }
            }
        )
    }

    if (showLocationDialog) {
        LocationDialog(
            initialLocations = locations,
            onDismiss = { showLocationDialog = false },
            onConfirm = { 
                locations.clear()
                locations.addAll(it)
                showLocationDialog = false
            }
        )
    }

    if (showParticipantsDialog) {
        ContactsSelectionDialog(
            actions = actions,
            selectedParticipants = participants,
            onDismiss = { showParticipantsDialog = false },
            onConfirm = { 
                participants.clear()
                participants.addAll(it)
                showParticipantsDialog = false
            }
        )
    }
}
