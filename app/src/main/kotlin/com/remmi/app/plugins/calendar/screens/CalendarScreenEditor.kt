package com.remmi.app.plugins.calendar.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.commands.CreateCalendarEventCommand
import com.remmi.app.core.eventBus.commands.UpdateCalendarEventCommand
import com.remmi.app.core.screens.components.*
import com.remmi.app.plugins.contacts.popups.ContactsSelectionDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.contacts.ContactActions
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import java.util.UUID

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
    Log.d("Remmi", "[CalendarScreenEditor] - Refactored for generic actions")
    val scope = rememberCoroutineScope()
    val initialEvent = (mode as? EditorMode.Edit)?.event

    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val initialDate = remember(initialEvent, mode) {
        initialEvent?.startingDate ?: (mode as? EditorMode.Create)?.initialDate ?: Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone).date
    }
    val initialTime = remember(initialEvent) {
        initialEvent?.startingTime ?: LocalTime(0, 0)
    }

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

    var contacts by remember { mutableStateOf(emptyList<com.remmi.app.plugins.contacts.ContactItem>()) }
    val contactActions = remember { controller.pluginManager.plugins["contacts"]?.actions as? ContactActions }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
        contacts = contactActions?.getAllContacts() ?: emptyList()
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
    
    var createLinkedTask by remember { mutableStateOf(initialEvent?.createTask ?: false) }
    var createLinkedAlarm by remember { mutableStateOf(initialEvent?.createAlarm ?: false) }
    
    val currentEventId = remember { initialEvent?.id ?: UUID.randomUUID().toString() }
    
    val locations = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.location ?: emptyList()) } }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    val participants = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.participants ?: emptyList()) } }
    
    val pickerRequestId = remember { "calendar_editor_${currentEventId}" }

    // Discover actions from other plugins
    val availableActions = remember(controller) {
        controller.pluginManager.plugins.values.flatMap { it.exposedActions }
    }

    // Listen for picked location
    LaunchedEffect(Unit) {
        controller.eventBus.events.collect { event ->
            if (event is com.remmi.app.core.eventBus.events.LocationPickedEvent && event.requestId == pickerRequestId) {
                val locStr = "${event.name} (${event.address ?: ""})"
                if (!locations.contains(locStr)) {
                    locations.add(locStr)
                }
            }
        }
    }

    RemmiEditorScaffold(
        title = if (initialEvent == null) "New Event" else "Edit Event",
        onBack = onDismiss,
        onSave = {
            val finalStartDate = try { LocalDate(year.toInt(), month.toInt(), day.toInt()) } catch (e: Exception) { startDate }
            
            scope.launch {
                if (initialEvent != null) {
                    controller.eventBus.publishCommand(
                        UpdateCalendarEventCommand(
                            event = initialEvent.copy(
                                modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                                title = title,
                                description = description,
                                startingDate = finalStartDate,
                                startingTime = startTime,
                                endingDate = endDate,
                                endingTime = endTime,
                                isPriority = isPriority,
                                group = group,
                                repeat = repeatList,
                                location = locations.toList(),
                                participants = participants.toList(),
                                createAlarm = createLinkedAlarm,
                                createTask = createLinkedTask
                            )
                        )
                    )
                } else {
                    controller.eventBus.publishCommand(
                        CreateCalendarEventCommand(
                            title = title,
                            description = description,
                            startingDate = finalStartDate,
                            startingTime = startTime,
                            endingDate = endDate,
                            endingTime = endTime,
                            isPriority = isPriority,
                            group = group,
                            participants = participants.toList(),
                            repeat = repeatList,
                            location = locations.toList(),
                            createLinkedTask = createLinkedTask,
                            createLinkedAlarm = createLinkedAlarm
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(2f))
        }

        RemmiPrioritySwitch(
            isPriority = isPriority,
            onPriorityChange = { isPriority = it },
            label = "Priority Event"
        )

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

        Text("Plugin Actions", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Generic Action Buttons from other plugins
            availableActions.forEach { action ->
                IconButton(onClick = { action.launch() }) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Still keeping hardcoded ones for direct control for now, or we migrate them fully
            IconButton(onClick = { showParticipantsDialog = true }) { Icon(Icons.Default.Person, "Participants") }
            IconButton(onClick = { 
                scope.launch {
                    controller.eventBus.publishCommand(
                        com.remmi.app.core.eventBus.commands.PickLocationCommand(
                            initialSearch = title.takeIf { it.isNotBlank() },
                            requestId = pickerRequestId
                        )
                    )
                }
            }) { Icon(Icons.Default.LocationOn, "Location") }
        }

        Text("Creation Intents", style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconToggleButton(
                checked = createLinkedTask,
                onCheckedChange = { createLinkedTask = it }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Link Task",
                    tint = if (createLinkedTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconToggleButton(
                checked = createLinkedAlarm,
                onCheckedChange = { createLinkedAlarm = it }
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Link Alarm",
                    tint = if (createLinkedAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
        RemmiDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { newDate ->
                startDate = newDate
                day = newDate.dayOfMonth.toString()
                month = newDate.monthNumber.toString()
                year = newDate.year.toString()
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

    if (showEndDatePicker) {
        RemmiDatePickerDialog(
            initialDate = endDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { newDate ->
                endDate = newDate
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
