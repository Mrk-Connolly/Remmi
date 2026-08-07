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
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.RepeatType
import com.remmi.app.plugins.tasks.TaskDialog
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID

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
    val initialDateTime = initialEvent?.startingTime?.toLocalDateTime(timeZone) ?: 
        (mode as? EditorMode.Create)?.initialDate?.atTime(LocalTime(0, 0)) ?:
        kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)

    var day by remember { mutableStateOf(initialDateTime.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(initialDateTime.monthNumber.toString()) }
    var year by remember { mutableStateOf(initialDateTime.year.toString()) }
    
    var priority by remember { mutableStateOf(initialEvent?.priority ?: Priority.NORMAL) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    var isRepeatable by remember { mutableStateOf(initialEvent?.repeat != null) }
    var repeatType by remember { mutableStateOf(initialEvent?.repeat?.type ?: RepeatType.NONE) }
    var repeatDays by remember { mutableStateOf(initialEvent?.repeat?.days ?: emptyList<DayOfWeek>()) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(initialDateTime.date) }
    var startTime by remember { mutableStateOf(initialDateTime.time) }
    var endDate by remember { mutableStateOf(initialEvent?.endingTime?.toLocalDateTime(timeZone)?.date ?: initialDateTime.date) }
    var endTime by remember { 
        mutableStateOf(
            initialEvent?.endingTime?.toLocalDateTime(timeZone)?.time ?: 
            initialDateTime.toInstant(timeZone).plus(1, DateTimeUnit.HOUR).toLocalDateTime(timeZone).time
        ) 
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var showTaskDialog by remember { mutableStateOf(false) }
    val linkedTaskIds = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.linkedTasks ?: emptyList()) } }
    val currentEventId = remember { initialEvent?.id ?: UUID.randomUUID().toString() }
    
    var showLocationDialog by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(initialEvent?.location) }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    val participants = remember { mutableStateListOf<com.remmi.app.core.model.models.Person>().apply { addAll(initialEvent?.participants ?: emptyList()) } }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(modifier = Modifier.weight(1f), onClick = onDismiss) { Text("Back") }
                    Button(modifier = Modifier.weight(1f), onClick = {
                        val start = LocalDateTime(year.toInt(), month.toInt(), day.toInt(), startTime.hour, startTime.minute).toInstant(timeZone)
                        val end = LocalDateTime(endDate.year, endDate.monthNumber, endDate.dayOfMonth, endTime.hour, endTime.minute).toInstant(timeZone)
                        val repeatRule = if (isRepeatable) RepeatRule(repeatType, repeatDays) else null
                        
                        scope.launch {
                            if (initialEvent != null) {
                                actions.updateEvent(initialEvent.copy(
                                    title = title,
                                    description = description,
                                    startingTime = start,
                                    endingTime = end,
                                    priority = priority,
                                    repeat = repeatRule,
                                    linkedTasks = linkedTaskIds,
                                    location = location,
                                    participants = participants
                                ))
                            } else {
                                actions.addEvent(
                                    id = currentEventId,
                                    title = title,
                                    description = description,
                                    startingTime = start,
                                    endingTime = end,
                                    priority = priority,
                                    repeat = repeatRule,
                                    linkedTasks = linkedTaskIds,
                                    location = location,
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
                                listOf(RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY).forEach { type ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = repeatType == type, onClick = { repeatType = type })
                                        Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = repeatType == RepeatType.CUSTOM, onClick = { 
                                    repeatType = RepeatType.CUSTOM
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
            selectedDays = repeatDays,
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { 
                repeatDays = it
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
                        val newDate = kotlinx.datetime.Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
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
                        endDate = kotlinx.datetime.Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
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
                    val newTaskId = UUID.randomUUID().toString()
                    val newTask = com.remmi.app.plugins.tasks.TaskItem(
                        id = newTaskId,
                        created = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        title = t,
                        description = d,
                        startingTime = s,
                        endingTime = e,
                        priority = p,
                        repeat = r,
                        completed = false,
                        linkedCalendarItem = currentEventId
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
            initialLocation = location,
            onDismiss = { showLocationDialog = false },
            onConfirm = { 
                location = it
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
    initialLocation: com.remmi.app.core.model.components.Location?,
    onDismiss: () -> Unit,
    onConfirm: (com.remmi.app.core.model.components.Location) -> Unit
) {
    var name by remember { mutableStateOf(initialLocation?.name ?: "") }
    var address by remember { mutableStateOf(initialLocation?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(com.remmi.app.core.model.components.Location(name, address)) }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ParticipantsDialog(
    onDismiss: () -> Unit,
    onAdd: (com.remmi.app.core.model.models.Person) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Participant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val person = com.remmi.app.core.model.models.Person(
                    id = UUID.randomUUID().toString(),
                    created = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                    modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                    name = com.remmi.app.core.model.components.PersonName(firstName, lastName),
                    contact = com.remmi.app.core.model.components.ContactInfo("")
                )
                onAdd(person)
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
    selectedDays: List<DayOfWeek>,
    onDismiss: () -> Unit,
    onConfirm: (List<DayOfWeek>) -> Unit
) {
    val days = DayOfWeek.entries
    val currentSelected = remember { mutableStateListOf<DayOfWeek>().apply { addAll(selectedDays) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                days.forEach { day ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().combinedClickable {
                        if (currentSelected.contains(day)) currentSelected.remove(day) else currentSelected.add(day)
                    }) {
                        Checkbox(checked = currentSelected.contains(day), onCheckedChange = {
                            if (it) currentSelected.add(day) else currentSelected.remove(day)
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

