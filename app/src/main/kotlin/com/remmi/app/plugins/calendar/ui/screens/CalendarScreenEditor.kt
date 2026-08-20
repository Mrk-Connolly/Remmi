package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.*
import com.remmi.app.core.screens.components.*
import com.remmi.app.core.screens.popups.LocationDialog
import com.remmi.app.core.screens.popups.ContactsSelectionDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.contacts.ContactActions
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.material3.MenuAnchorType

sealed class EditorMode {
    data class Create(
        val initialDate: LocalDate? = null,
        val initialTime: LocalTime? = null,
        val initialEndDate: LocalDate? = null,
        val initialEndTime: LocalTime? = null
    ) : EditorMode()
    data class Edit(val event: CalendarItem) : EditorMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEditorScreen(
    mode: EditorMode,
    actions: CalendarActions,
    controller: RemmiController,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[CalendarScreenEditor] - Refactored")
    val scope = rememberCoroutineScope()
    val initialEvent = (mode as? EditorMode.Edit)?.event

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val initialDate = remember(initialEvent, mode) {
        initialEvent?.startingDate ?: (mode as? EditorMode.Create)?.initialDate ?: Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone).date
    }
    val initialTime = remember(initialEvent, mode) {
        initialEvent?.startingTime ?: (mode as? EditorMode.Create)?.initialTime ?: LocalTime(0, 0)
    }

    var group by remember { mutableStateOf(initialEvent?.group) }

    var existingGroups by remember { mutableStateOf(emptyList<String>()) }
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    var contacts by remember { mutableStateOf(emptyList<com.remmi.app.plugins.contacts.ContactItem>()) }
    val contactActions = remember { controller.pluginManager.plugins["contacts"]?.actions as? ContactActions }
    val alarmActions = remember { controller.pluginManager.plugins["alarm"]?.actions as? AlarmActions }
    val tasksActions = remember { controller.pluginManager.plugins["tasks"]?.actions as? TasksActions }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
        contacts = contactActions?.getAllContacts() ?: emptyList()
    }

    var repeatList by remember { mutableStateOf(initialEvent?.repeat ?: emptyList<String>()) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf(initialTime) }
    var endDate by remember { 
        mutableStateOf(initialEvent?.endingDate ?: (mode as? EditorMode.Create)?.initialEndDate ?: initialDate) 
    }
    var endTime by remember { 
        mutableStateOf(initialEvent?.endingTime ?: (mode as? EditorMode.Create)?.initialEndTime ?: LocalTime(23, 59)) 
    }
    
    val linkedTaskIds = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.linkedTasks ?: emptyList()) } }

    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var autoCreateTask by remember { mutableStateOf(false) }
    var autoCreateAlarm by remember { mutableStateOf(false) }
    
    var showLocationDialog by remember { mutableStateOf(false) }
    val locations = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.location ?: emptyList()) } }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    val participants = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.participants ?: emptyList()) } }
    
    val isEndDateGreyedOut = remember(startDate, endDate) { startDate == endDate }

    RemmiEditorScaffold(
        title = if (initialEvent == null) "New Event" else "Edit Event",
        onBack = onDismiss,
        onSave = {
            scope.launch {
                val finalEvent = if (initialEvent != null) {
                    initialEvent.copy(
                        modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        title = title,
                        description = description,
                        startingDate = startDate,
                        startingTime = startTime,
                        endingDate = endDate,
                        endingTime = endTime,
                        isPriority = isPriority,
                        group = group,
                        repeat = repeatList,
                        linkedTasks = linkedTaskIds.toList(),
                        location = locations.toList(),
                        participants = participants.toList()
                    )
                } else {
                    CalendarItem(
                        id = UUID.randomUUID().toString(),
                        created = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()),
                        title = title,
                        description = description,
                        startingDate = startDate,
                        startingTime = startTime,
                        endingDate = endDate,
                        endingTime = endTime,
                        isPriority = isPriority,
                        group = group,
                        participants = participants.toList(),
                        repeat = repeatList,
                        location = locations.toList(),
                        linkedTasks = linkedTaskIds.toList()
                    )
                }

                if (initialEvent != null) {
                    controller.eventBus.publishCommand(UpdateCalendarEventCommand(event = finalEvent))
                } else {
                    controller.eventBus.publishCommand(
                        CreateCalendarEventCommand(
                            title = finalEvent.title,
                            description = finalEvent.description,
                            startingDate = finalEvent.startingDate,
                            startingTime = finalEvent.startingTime,
                            endingDate = finalEvent.endingDate,
                            endingTime = finalEvent.endingTime,
                            isPriority = finalEvent.isPriority,
                            group = finalEvent.group,
                            participants = finalEvent.participants,
                            repeat = finalEvent.repeat,
                            location = finalEvent.location,
                            linkedTasks = finalEvent.linkedTasks
                        )
                    )
                }

                if (autoCreateTask) {
                    controller.eventBus.publishCommand(
                        CreateTaskCommand(
                            title = "Task: $title",
                            description = description,
                            dueDate = startDate.atTime(startTime).toInstant(timeZone),
                            isPriority = isPriority,
                            group = group
                        )
                    )
                }

                if (autoCreateAlarm) {
                    val alarmTime = startDate.atTime(startTime).toInstant(timeZone)
                    controller.eventBus.publishCommand(
                        CreateAlarmCommand(
                            title = "Alarm: $title",
                            description = description,
                            time = alarmTime,
                            isPriority = isPriority
                        )
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

        // Date and Time Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = startTime.toString().substring(0, 5),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Time") },
                    modifier = Modifier.weight(0.6f).clickable { showStartTimePicker = true }
                )
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, "Select Date Range")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val textColor = if (isEndDateGreyedOut) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface
                OutlinedTextField(
                    value = endDate.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endTime.toString().substring(0, 5),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Time") },
                    modifier = Modifier.weight(0.6f).clickable { showEndTimePicker = true }
                )
                IconButton(onClick = { showEndDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, "Select End Date")
                }
            }
        }

        // Group Selection
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
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
            label = "Priority Event"
        )

        // Action Buttons Row (Consolidated)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Toggle
            IconButton(
                onClick = { autoCreateTask = !autoCreateTask }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = "Add Task",
                    tint = if (autoCreateTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Alarm Toggle
            IconButton(
                onClick = { autoCreateAlarm = !autoCreateAlarm }
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Add Alarm",
                    tint = if (autoCreateAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Participants
            IconButton(onClick = { showParticipantsDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Participants",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location
            IconButton(onClick = { showLocationDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        RemmiDaySelectionDialog(
            selectedDays = repeatList,
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { 
                repeatList = it
                showRepeatDaysDialog = false
            }
        )
    }


    if (showStartDatePicker) {
        RemmiDateRangePickerDialog(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onDismiss = { showStartDatePicker = false },
            onRangeSelected = { start, end ->
                startDate = start
                endDate = end
            }
        )
    }

    if (showEndDatePicker) {
        RemmiDatePickerDialog(
            initialDate = endDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { newDate ->
                endDate = newDate
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

    if (showEndTimePicker) {
        RemmiTimePickerDialog(
            initialTime = endTime,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = { time ->
                endTime = time
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
            contacts = contacts,
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
