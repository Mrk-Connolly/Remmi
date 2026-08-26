package com.remmi.app.plugins.calendar.ui.screens

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
import com.remmi.app.core.controller.LinkedCreationData
import com.remmi.app.core.eventBus.commands.CreateCalendarEventCommand
import com.remmi.app.core.eventBus.commands.UpdateCalendarEventCommand
import com.remmi.app.core.screens.components.*
import com.remmi.app.plugins.contacts.popups.ContactsSelectionDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactItem
import com.remmi.app.core.controller.GlobalUIState
import kotlinx.datetime.*
import kotlinx.datetime.Clock
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
    Log.d("Remmi", "[CalendarEditorScreen] - Initializing")
    val scope = rememberCoroutineScope()
    val initialEvent = (mode as? EditorMode.Edit)?.event

    // --- Mandatory Fields ---
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val baseDate = remember(initialEvent, mode) {
        initialEvent?.startingDate ?: (mode as? EditorMode.Create)?.initialDate ?: Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(timeZone).date
    }
    var day by remember { mutableStateOf(baseDate.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(baseDate.monthNumber.toString()) }
    var year by remember { mutableStateOf(baseDate.year.toString()) }

    // --- Mandatory w/ Defaults ---
    var startDate by remember { mutableStateOf(baseDate) }
    var endDate by remember { mutableStateOf(initialEvent?.endingDate ?: baseDate) }
    var startTime by remember { mutableStateOf(initialEvent?.startingTime ?: LocalTime(9, 0)) }
    var endTime by remember { mutableStateOf(initialEvent?.endingTime ?: LocalTime(10, 0)) }
    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    var group by remember { mutableStateOf(initialEvent?.group) }
    var isRepeatable by remember { mutableStateOf(initialEvent?.isRepeatable ?: false) }

    // --- Optional Fields ---
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var repeatableType by remember { mutableStateOf(initialEvent?.repeatableType) }
    var createAlarm by remember { mutableStateOf(initialEvent?.createAlarm ?: false) }
    var createTask by remember { mutableStateOf(initialEvent?.createTask ?: false) }
    var createLocation by remember { mutableStateOf(initialEvent?.createLocation ?: false) }
    
    val participants = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.participants ?: emptyList()) } }
    val locations = remember { mutableStateListOf<String>().apply { addAll(initialEvent?.location ?: emptyList()) } }

    // --- UI State ---
    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var existingGroups by remember { mutableStateOf(emptyList<String>()) }
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    
    val currentEventId = remember { initialEvent?.id ?: UUID.randomUUID().toString() }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }

    val contactActions = remember { controller.pluginManager.plugins["contacts"]?.actions as? ContactActions }
    var contacts by remember { mutableStateOf<List<ContactItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
        contacts = contactActions?.getAllContacts() ?: emptyList()
    }

    // Listen for picked location
    LaunchedEffect(Unit) {
        controller.eventBus.events.collect { event ->
            if (event is com.remmi.app.core.eventBus.events.LocationPickedEvent && event.requestId == currentEventId) {
                val locStr = "${event.name} (${event.address ?: ""})"
                if (!locations.contains(locStr)) {
                    locations.add(locStr)
                }
            }
        }
    }

    // Correlation/Causation ID for linked items
    val correlationId = remember { UUID.randomUUID().toString() }

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
                                title = title,
                                description = description,
                                startingDate = finalStartDate,
                                startingTime = startTime,
                                endingDate = endDate,
                                endingTime = endTime,
                                isPriority = isPriority,
                                group = group,
                                isRepeatable = isRepeatable,
                                repeatableType = repeatableType,
                                participants = participants.toList(),
                                location = locations.toList(),
                                createAlarm = createAlarm,
                                createTask = createTask,
                                createLocation = createLocation,
                                createContact = participants.isNotEmpty()
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
                            isRepeatable = isRepeatable,
                            repeatableType = repeatableType,
                            participants = participants.toList(),
                            location = locations.toList(),
                            createLinkedAlarm = createAlarm,
                            createLinkedTask = createTask,
                            createLinkedLocation = createLocation,
                            createLinkedContact = participants.isNotEmpty()
                        )
                    )
                }
                onSave()
            }
        },
        saveEnabled = title.isNotBlank()
    ) {
        // --- Main Inputs ---
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

        // --- Group Selection ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                ExposedDropdownMenu(expanded = isGroupExpanded, onDismissRequest = { isGroupExpanded = false }) {
                    DropdownMenuItem(text = { Text("No Group") }, onClick = { group = null; isGroupExpanded = false })
                    existingGroups.forEach { g ->
                        DropdownMenuItem(text = { Text(g) }, onClick = { group = g; isGroupExpanded = false })
                    }
                }
            }
            IconButton(onClick = { showAddGroupDialog = true }) { Icon(Icons.Default.Add, "New Group") }
        }

        // --- Linked Items Buttons (The 4 Icon Buttons) ---
        Text("Link Items", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val buttonModifier = Modifier.weight(1f).height(48.dp)
            
            // Alarm Button
            IconToggleButton(
                checked = createAlarm,
                onCheckedChange = { 
                    createAlarm = it
                    if (it) {
                        GlobalUIState.pendingAlarmRequest.value = LinkedCreationData(
                            title = "Alarm: $title",
                            description = description,
                            sourcePlugin = "calendar",
                            sourceItemId = currentEventId,
                            correlationId = correlationId,
                            causationId = "calendar_ui"
                        )
                    }
                },
                modifier = buttonModifier
            ) {
                Icon(Icons.Default.Alarm, "Link Alarm", tint = if (createAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Task Button
            IconToggleButton(
                checked = createTask,
                onCheckedChange = { 
                    createTask = it
                    if (it) {
                        GlobalUIState.pendingTaskRequest.value = LinkedCreationData(
                            title = "Task: $title",
                            description = description,
                            sourcePlugin = "calendar",
                            sourceItemId = currentEventId,
                            correlationId = correlationId,
                            causationId = "calendar_ui"
                        )
                    }
                },
                modifier = buttonModifier
            ) {
                Icon(Icons.Default.CheckCircle, "Link Task", tint = if (createTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Location Button
            IconToggleButton(
                checked = createLocation,
                onCheckedChange = { 
                    createLocation = it
                    if (it) {
                        GlobalUIState.showLocationPicker.value = true
                        GlobalUIState.locationPickerData.value = LinkedCreationData(
                            title = title,
                            description = description,
                            sourcePlugin = "calendar",
                            sourceItemId = currentEventId,
                            correlationId = correlationId,
                            causationId = "calendar_ui"
                        )
                    }
                },
                modifier = buttonModifier
            ) {
                Icon(Icons.Default.LocationOn, "Link Location", tint = if (createLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Contact Button (Existing implementation)
            IconButton(
                onClick = { showParticipantsDialog = true },
                modifier = buttonModifier
            ) {
                Icon(Icons.Default.Person, "Link Contact", tint = if (participants.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // --- Advanced Options ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { isAdvancedExpanded = !isAdvancedExpanded }
        ) {
            Text("Advanced Options", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Icon(if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
        }

        if (isAdvancedExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                    Text("Repeatable")
                }
                if (isRepeatable) {
                   Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = repeatableType == type, onClick = { repeatableType = type })
                                Text(type.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                            }
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

    // --- Dialogs ---
    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Add New Group") },
            text = { OutlinedTextField(value = newGroupName, onValueChange = { newGroupName = it }, label = { Text("Group Name") }) },
            confirmButton = { Button(onClick = { if (newGroupName.isNotBlank()) { group = newGroupName; showAddGroupDialog = false; newGroupName = "" } }) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAddGroupDialog = false }) { Text("Cancel") } }
        )
    }

    if (showStartDatePicker) {
        RemmiDatePickerDialog(initialDate = startDate, onDismiss = { showStartDatePicker = false }, onDateSelected = { newDate ->
            startDate = newDate
            day = newDate.dayOfMonth.toString()
            month = newDate.monthNumber.toString()
            year = newDate.year.toString()
        })
    }

    if (showStartTimePicker) {
        RemmiTimePickerDialog(initialTime = startTime, onDismiss = { showStartTimePicker = false }, onTimeSelected = { startTime = it })
    }

    if (showEndDatePicker) {
        RemmiDatePickerDialog(initialDate = endDate, onDismiss = { showEndDatePicker = false }, onDateSelected = { endDate = it })
    }

    if (showEndTimePicker) {
        RemmiTimePickerDialog(initialTime = endTime, onDismiss = { showEndTimePicker = false }, onTimeSelected = { endTime = it })
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
