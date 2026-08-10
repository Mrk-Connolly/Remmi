package com.remmi.app.plugins.calendar

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
import androidx.compose.ui.window.Dialog
import com.remmi.app.core.model.components.Priority
import com.remmi.app.plugins.tasks.TaskDialog
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID

sealed class EditorMode {
    data class Create(val initialDate: LocalDate? = null) : EditorMode()
    data class Edit(val event: CalendarItem) : EditorMode()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarEditorScreen(
    mode: EditorMode,
    actions: CalendarActions,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
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
    
    var priority by remember { mutableStateOf(initialEvent?.priority ?: Priority.Normal) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var isRepeatable by remember { mutableStateOf(initialEvent?.repeat?.isNotEmpty() == true) }
    var repeatList by remember { mutableStateOf(initialEvent?.repeat ?: emptyList<String>()) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialTime) }
    var endDate by remember { mutableStateOf(initialEvent?.endingDate ?: initialDate) }
    var endTime by remember { 
        val defaultEnd = if (initialTime.hour < 23) LocalTime(initialTime.hour + 1, initialTime.minute) else LocalTime(23, 59)
        mutableStateOf(initialEvent?.endingTime ?: defaultEnd) 
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

    Scaffold(
        bottomBar = {
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
                                    priority = priority,
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
                                    priority = priority,
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("DAILY", "WEEKLY", "MONTHLY").forEach { type ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = repeatList.contains(type), onClick = { repeatList = listOf(type) })
                                        Text(type.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = repeatList.size > 1 || (repeatList.isNotEmpty() && !listOf("DAILY", "WEEKLY", "MONTHLY").contains(repeatList[0])), onClick = { 
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton(onClick = { showTaskDialog = true }) { Icon(Icons.Default.CheckCircle, "Task") }
                        IconButton(onClick = { /* Alarm TODO */ }) { Icon(Icons.Default.Alarm, "Alarm") }
                        IconButton(onClick = { showParticipantsDialog = true }) { Icon(Icons.Default.Person, "Participants") }
                        IconButton(onClick = { showLocationDialog = true }) { Icon(Icons.Default.LocationOn, "Location") }
                    }
                }
            }
        }
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
            onDismiss = { showTaskDialog = false },
            onSave = { t, d, s, e, p, r, sync ->
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
                        priority = p,
                        completed = false,
                        linkedCalendar = currentEventId
                    )
                    actions.addTask(newTask)
                    linkedTaskIds.add(newTaskId)
                    showTaskDialog = false
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
        ParticipantsDialog(
            onDismiss = { showParticipantsDialog = false },
            onAdd = { participants.add(it) }
        )
    }
}

@Composable
fun LocationDialog(
    initialLocations: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val currentLocations = remember { mutableStateListOf<String>().apply { addAll(initialLocations) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Location Name") }, modifier = Modifier.fillMaxWidth())
                currentLocations.forEach { loc ->
                    Text(text = loc, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { if (name.isNotBlank()) currentLocations.add(name); name = "" }) { Text("Add") }
                Button(onClick = { onConfirm(currentLocations.toList()) }) { Text("Confirm") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ParticipantsDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Participant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onAdd(name)
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RepeatDaysDialog(
    selectedDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val days = DayOfWeek.entries
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedDays) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                days.forEach { day ->
                    val dayStr = day.name
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable {
                        if (currentSelected.contains(dayStr)) currentSelected.remove(dayStr) else currentSelected.add(dayStr)
                    }) {
                        Checkbox(checked = currentSelected.contains(dayStr), onCheckedChange = {
                            if (it) currentSelected.add(dayStr) else currentSelected.remove(dayStr)
                        })
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentSelected.toList()) }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
