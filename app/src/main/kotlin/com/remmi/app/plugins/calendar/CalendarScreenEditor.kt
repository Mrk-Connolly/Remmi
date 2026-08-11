package com.remmi.app.plugins.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.remmi.app.core.model.components.Priority
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID

sealed class EditorMode {
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        IconButton(onClick = { showTaskDialog = true }) { Icon(Icons.Default.CheckCircle, "Task") }
                        IconButton(onClick = { showAlarmConfirmation = true }) { Icon(Icons.Default.Alarm, "Alarm") }
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
                        priority = p,
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
        AlertDialog(
            onDismissRequest = { showAlarmConfirmation = false },
            title = { Text("Create Alarm") },
            text = { Text("Do you want to create an alarm for this event?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val alarmActions = actions.getAlarmActions()
                        if (alarmActions != null) {
                            val eventTime = startDate.atTime(startTime).toInstant(timeZone)
                            alarmActions.addAlarm(
                                title = title,
                                description = description,
                                time = eventTime,
                                priority = priority
                            )
                        }
                        showAlarmConfirmation = false
                    }
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmConfirmation = false }) { Text("No") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialDate: LocalDate? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Instant?, Priority, com.remmi.app.core.model.components.RepeatRule?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDescription) }
    var priority by remember { mutableStateOf(Priority.Normal) }
    
    val timeZone = TimeZone.currentSystemDefault()
    var dueDate by remember { mutableStateOf(initialDate ?: Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone).date) }
    var showDatePicker by remember { mutableStateOf(false) }

    var isRepeatable by remember { mutableStateOf(false) }
    var repeatType by remember { mutableStateOf(com.remmi.app.core.model.components.RepeatType.DAILY) }
    
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
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(
                    value = dueDate.toString(),
                    onValueChange = {},
                    label = { Text("Due Date") },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    readOnly = true
                )

                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Priority.entries.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = priority == p, onClick = { priority = p })
                            Text(p.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                    Text("Repeat")
                }

                if (isRepeatable) {
                    Column {
                        listOf(
                            com.remmi.app.core.model.components.RepeatType.DAILY,
                            com.remmi.app.core.model.components.RepeatType.WEEKLY,
                            com.remmi.app.core.model.components.RepeatType.MONTHLY,
                            com.remmi.app.core.model.components.RepeatType.YEARLY
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
                    val dueInstant = dueDate.atTime(0, 0).toInstant(timeZone)
                    val repeatRule = if (isRepeatable) com.remmi.app.core.model.components.RepeatRule(repeatType) else null
                    onSave(title, desc, dueInstant, priority, repeatRule) 
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSelectionDialog(
    actions: CalendarActions,
    selectedParticipants: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf(emptyList<com.remmi.app.plugins.contacts.ContactItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("All") }
    var isGroupDropdownExpanded by remember { mutableStateOf(false) }
    
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedParticipants) } }

    LaunchedEffect(Unit) {
        scope.launch {
            contacts = actions.getContactActions()?.getAllContacts() ?: emptyList()
        }
    }

    val groups = listOf("All") + contacts.map { it.group }.distinct()
    val filteredContacts = contacts.filter { 
        (selectedGroup == "All" || it.group == selectedGroup) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.surname.contains(searchQuery, ignoreCase = true))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Participants") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                
                ExposedDropdownMenuBox(
                    expanded = isGroupDropdownExpanded,
                    onExpandedChange = { isGroupDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Group") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGroupDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isGroupDropdownExpanded,
                        onDismissRequest = { isGroupDropdownExpanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedGroup = group
                                    isGroupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(filteredContacts) { contact ->
                        val fullName = "${contact.name} ${contact.surname}"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (currentSelected.contains(fullName)) currentSelected.remove(fullName)
                                else currentSelected.add(fullName)
                            }
                        ) {
                            Checkbox(
                                checked = currentSelected.contains(fullName),
                                onCheckedChange = {
                                    if (it) currentSelected.add(fullName)
                                    else currentSelected.remove(fullName)
                                }
                            )
                            Text("${contact.name} ${contact.surname} (${contact.group})")
                        }
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
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
