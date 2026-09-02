package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.LinkedCreationData
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiAddScreen
import com.remmi.app.ui.components.RemmiModifyScreen
import com.remmi.app.ui.popups.RemmiDatePickerDialog
import com.remmi.app.ui.popups.RemmiLinkedActionButton
import com.remmi.app.ui.popups.RemmiTimePickerDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.ui.popups.NewGroupDialog
import com.remmi.app.plugins.calendar.ui.popups.ParticipantsPopup
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarScreenEditor(
    mode: CalendarEditorMode,
    actions: CalendarActions,
    controller: RemmiController,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[CalendarScreenEditor] - [CalendarScreenEditor] executed")
    val scope = rememberCoroutineScope()
    
    val initialEvent = (mode as? CalendarEditorMode.Edit)?.event
    val initialDate = (mode as? CalendarEditorMode.CreateOnDate)?.date
    
    val today = remember { 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }
    
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    
    // Group State
    var groupName by remember { mutableStateOf(initialEvent?.group ?: "") }
    var groups by remember { mutableStateOf(emptyList<CalendarGroup>()) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    // Date/Time State
    var startingDate by remember { mutableStateOf(initialEvent?.startingDate ?: initialDate ?: today) }
    var startingTime by remember { 
        mutableStateOf(initialEvent?.startingTime ?: (mode as? CalendarEditorMode.CreateOnDate)?.startTime ?: LocalTime(9, 0)) 
    }
    var endingDate by remember { mutableStateOf(initialEvent?.endingDate ?: startingDate) }
    var endingTime by remember { 
        mutableStateOf(initialEvent?.endingTime ?: (mode as? CalendarEditorMode.CreateOnDate)?.endTime ?: LocalTime(10, 0)) 
    }
    
    // Repeat State
    var isRepeatable by remember { mutableStateOf(initialEvent?.isRepeatable ?: false) }
    var repeatableType by remember { mutableStateOf(initialEvent?.repeatableType ?: "None") }

    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    
    // Linked Creation Flags
    var createAlarm by remember { mutableStateOf(initialEvent?.createAlarm ?: false) }
    var alarmCorrelationId by remember { mutableStateOf<String?>(null) }
    
    var createTask by remember { mutableStateOf(initialEvent?.createTask ?: false) }
    var taskCorrelationId by remember { mutableStateOf<String?>(null) }
    
    var createLocation by remember { mutableStateOf(initialEvent?.createLocation ?: false) }
    var locationCorrelationId by remember { mutableStateOf<String?>(null) }
    
    var createContact by remember { mutableStateOf(initialEvent?.createContact ?: false) }

    var participants by remember { mutableStateOf(emptyList<String>()) }
    var showParticipantsPopup by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            groups = actions.getCalendarGroups()
        }
    }

    // Rollback logic for linked actions if canceled
    LaunchedEffect(com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value) {
        val confirmedId = com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value
        if (confirmedId != null) { }
    }

    LaunchedEffect(com.remmi.app.core.controller.GlobalUIState.pendingAlarmRequest.value) {
        if (com.remmi.app.core.controller.GlobalUIState.pendingAlarmRequest.value == null && alarmCorrelationId != null) {
            if (com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value != alarmCorrelationId) {
                createAlarm = false
                alarmCorrelationId = null
            }
        }
    }

    LaunchedEffect(com.remmi.app.core.controller.GlobalUIState.pendingTaskRequest.value) {
        if (com.remmi.app.core.controller.GlobalUIState.pendingTaskRequest.value == null && taskCorrelationId != null) {
            if (com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value != taskCorrelationId) {
                createTask = false
                taskCorrelationId = null
            }
        }
    }

    LaunchedEffect(com.remmi.app.core.controller.GlobalUIState.showLocationPicker.value) {
        if (!com.remmi.app.core.controller.GlobalUIState.showLocationPicker.value && locationCorrelationId != null) {
            if (com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value != locationCorrelationId) {
                createLocation = false
                locationCorrelationId = null
            }
        }
    }

    if (initialEvent == null) {
        RemmiAddScreen(
            title = "New Event",
            onBack = onDismiss,
            onSave = {
                scope.launch {
                    actions.addEvent(
                        title = title,
                        description = description,
                        startingDate = startingDate,
                        startingTime = startingTime,
                        endingDate = endingDate,
                        endingTime = endingTime,
                        isPriority = isPriority,
                        group = if (groupName == "None" || groupName.isEmpty()) null else groupName,
                        isRepeatable = isRepeatable,
                        repeatableType = if (repeatableType == "None") null else repeatableType,
                        createAlarm = createAlarm,
                        createTask = createTask,
                        createLocation = createLocation,
                        createContact = createContact
                    )
                    onSave()
                }
            },
            saveEnabled = title.isNotEmpty()
        ) {
            EditorContent(
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                groupName = groupName, onGroupNameChange = { groupName = it },
                groups = groups,
                onAddNewGroup = { showNewGroupDialog = true },
                startingDate = startingDate, onStartingDateChange = { startingDate = it },
                startingTime = startingTime, onStartingTimeChange = { startingTime = it },
                endingDate = endingDate, onEndingDateChange = { endingDate = it },
                endingTime = endingTime, onEndingTimeChange = { endingTime = it },
                isRepeatable = isRepeatable, onIsRepeatableChange = { isRepeatable = it },
                repeatableType = repeatableType, onRepeatableTypeChange = { repeatableType = it },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                createAlarm = createAlarm, onCreateAlarmChange = { createAlarm = it },
                alarmCorrelationId = alarmCorrelationId, onAlarmCorrelationIdChange = { alarmCorrelationId = it },
                createTask = createTask, onCreateTaskChange = { createTask = it },
                taskCorrelationId = taskCorrelationId, onTaskCorrelationIdChange = { taskCorrelationId = it },
                createLocation = createLocation, onCreateLocationChange = { createLocation = it },
                locationCorrelationId = locationCorrelationId, onLocationCorrelationIdChange = { locationCorrelationId = it },
                createContact = createContact, onCreateContactChange = { createContact = it },
                participants = participants,
                onShowParticipantsPopup = { showParticipantsPopup = true },
                onShowStartDatePicker = { showStartDatePicker = true },
                onShowStartTimePicker = { showStartTimePicker = true },
                onShowEndDatePicker = { showEndDatePicker = true },
                onShowEndTimePicker = { showEndTimePicker = true },
                initialEventId = initialEvent?.id
            )
        }
    } else {
        RemmiModifyScreen(
            title = "Edit Event",
            onBack = onDismiss,
            onDelete = {
                scope.launch {
                    actions.removeEvent(initialEvent.id)
                    onSave()
                }
            },
            onSave = {
                scope.launch {
                    val item = initialEvent.copy(
                        title = title,
                        description = description,
                        startingDate = startingDate,
                        startingTime = startingTime,
                        endingDate = endingDate,
                        endingTime = endingTime,
                        isPriority = isPriority,
                        group = if (groupName == "None" || groupName.isEmpty()) null else groupName,
                        isRepeatable = isRepeatable,
                        repeatableType = if (repeatableType == "None") null else repeatableType,
                        createAlarm = createAlarm,
                        createTask = createTask,
                        createLocation = createLocation,
                        createContact = createContact,
                        modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                    )
                    actions.updateEvent(item)
                    onSave()
                }
            },
            saveEnabled = title.isNotEmpty()
        ) {
            EditorContent(
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                groupName = groupName, onGroupNameChange = { groupName = it },
                groups = groups,
                onAddNewGroup = { showNewGroupDialog = true },
                startingDate = startingDate, onStartingDateChange = { startingDate = it },
                startingTime = startingTime, onStartingTimeChange = { startingTime = it },
                endingDate = endingDate, onEndingDateChange = { endingDate = it },
                endingTime = endingTime, onEndingTimeChange = { endingTime = it },
                isRepeatable = isRepeatable, onIsRepeatableChange = { isRepeatable = it },
                repeatableType = repeatableType, onRepeatableTypeChange = { repeatableType = it },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                createAlarm = createAlarm, onCreateAlarmChange = { createAlarm = it },
                alarmCorrelationId = alarmCorrelationId, onAlarmCorrelationIdChange = { alarmCorrelationId = it },
                createTask = createTask, onCreateTaskChange = { createTask = it },
                taskCorrelationId = taskCorrelationId, onTaskCorrelationIdChange = { taskCorrelationId = it },
                createLocation = createLocation, onCreateLocationChange = { createLocation = it },
                locationCorrelationId = locationCorrelationId, onLocationCorrelationIdChange = { locationCorrelationId = it },
                createContact = createContact, onCreateContactChange = { createContact = it },
                participants = participants,
                onShowParticipantsPopup = { showParticipantsPopup = true },
                onShowStartDatePicker = { showStartDatePicker = true },
                onShowStartTimePicker = { showStartTimePicker = true },
                onShowEndDatePicker = { showEndDatePicker = true },
                onShowEndTimePicker = { showEndTimePicker = true },
                initialEventId = initialEvent.id
            )
        }
    }

    if (showParticipantsPopup) {
        ParticipantsPopup(
            initialParticipants = participants,
            onDismiss = { showParticipantsPopup = false },
            onConfirmed = { participants = it }
        )
    }

    if (showNewGroupDialog) {
        NewGroupDialog(
            onDismiss = { showNewGroupDialog = false },
            onSave = { name, color ->
                scope.launch {
                    actions.addCalendarGroup(name, color)
                    groups = actions.getCalendarGroups()
                    groupName = name
                    showNewGroupDialog = false
                }
            }
        )
    }

    if (showStartDatePicker) {
        RemmiDatePickerDialog(initialDate = startingDate, onDismiss = { showStartDatePicker = false }, onDateSelected = { startingDate = it })
    }
    if (showStartTimePicker) {
        RemmiTimePickerDialog(initialTime = startingTime, onDismiss = { showStartTimePicker = false }, onTimeSelected = { startingTime = it })
    }
    if (showEndDatePicker) {
        RemmiDatePickerDialog(initialDate = endingDate, onDismiss = { showEndDatePicker = false }, onDateSelected = { endingDate = it })
    }
    if (showEndTimePicker) {
        RemmiTimePickerDialog(initialTime = endingTime, onDismiss = { showEndTimePicker = false }, onTimeSelected = { endingTime = it })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorContent(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    groupName: String, onGroupNameChange: (String) -> Unit,
    groups: List<CalendarGroup>,
    onAddNewGroup: () -> Unit,
    startingDate: LocalDate, onStartingDateChange: (LocalDate) -> Unit,
    startingTime: LocalTime, onStartingTimeChange: (LocalTime) -> Unit,
    endingDate: LocalDate, onEndingDateChange: (LocalDate) -> Unit,
    endingTime: LocalTime, onEndingTimeChange: (LocalTime) -> Unit,
    isRepeatable: Boolean, onIsRepeatableChange: (Boolean) -> Unit,
    repeatableType: String, onRepeatableTypeChange: (String) -> Unit,
    isPriority: Boolean, onIsPriorityChange: (Boolean) -> Unit,
    createAlarm: Boolean, onCreateAlarmChange: (Boolean) -> Unit,
    alarmCorrelationId: String?, onAlarmCorrelationIdChange: (String?) -> Unit,
    createTask: Boolean, onCreateTaskChange: (Boolean) -> Unit,
    taskCorrelationId: String?, onTaskCorrelationIdChange: (String?) -> Unit,
    createLocation: Boolean, onCreateLocationChange: (Boolean) -> Unit,
    locationCorrelationId: String?, onLocationCorrelationIdChange: (String?) -> Unit,
    createContact: Boolean, onCreateContactChange: (Boolean) -> Unit,
    participants: List<String>,
    onShowParticipantsPopup: () -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    initialEventId: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Group", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val selectedGroup = groups.find { it.name == groupName }
                            if (selectedGroup != null) {
                                Box(Modifier.size(14.dp).background(Color(android.graphics.Color.parseColor(selectedGroup.colorHex)), CircleShape))
                                Spacer(Modifier.width(12.dp))
                            } else {
                                Box(Modifier.size(14.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
                                Spacer(Modifier.width(12.dp))
                            }
                            Text(groupName.ifEmpty { "None" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                    DropdownMenu(
                        expanded = expanded, 
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") }, 
                            onClick = { onGroupNameChange(""); expanded = false }
                        )
                        groups.forEach { g ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).background(Color(android.graphics.Color.parseColor(g.colorHex)), CircleShape))
                                        Spacer(Modifier.width(12.dp))
                                        Text(g.name)
                                    }
                                },
                                onClick = { onGroupNameChange(g.name); expanded = false }
                            )
                        }
                    }
                }
                
                FilledIconButton(
                    onClick = onAddNewGroup,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Group", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Start", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            DateFieldRow(date = startingDate, onDateChange = onStartingDateChange, onIconClick = onShowStartDatePicker)
            TimeFieldRow(time = startingTime, onTimeChange = onStartingTimeChange, onIconClick = onShowStartTimePicker)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("End", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            DateFieldRow(date = endingDate, onDateChange = onEndingDateChange, onIconClick = onShowEndDatePicker)
            TimeFieldRow(time = endingTime, onTimeChange = onEndingTimeChange, onIconClick = onShowEndTimePicker)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isRepeatable, onCheckedChange = onIsRepeatableChange)
                Text("Repeat Event", fontWeight = FontWeight.Bold)
            }
            if (isRepeatable) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { type ->
                        FilterChip(
                            selected = repeatableType == type,
                            onClick = { onRepeatableTypeChange(type) },
                            label = { Text(type) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPriority, onCheckedChange = onIsPriorityChange)
            Text("Priority Event", fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), 
            horizontalArrangement = Arrangement.Center
        ) {
            RemmiLinkedActionButton(
                icon = Icons.Default.Alarm,
                active = createAlarm,
                onClick = { 
                    onCreateAlarmChange(!createAlarm)
                    if (!createAlarm) {
                        val cid = UUID.randomUUID().toString()
                        onAlarmCorrelationIdChange(cid)
                        GlobalUIState.pendingAlarmRequest.value = LinkedCreationData(
                            title = title, description = description, sourcePlugin = "calendar",
                            sourceItemId = initialEventId ?: "draft", correlationId = cid, causationId = null
                        )
                    } else {
                        onAlarmCorrelationIdChange(null)
                    }
                }
            )
            
            Spacer(Modifier.width(32.dp))

            RemmiLinkedActionButton(
                icon = Icons.Default.CheckCircle,
                active = createTask,
                onClick = { 
                    onCreateTaskChange(!createTask)
                    if (!createTask) {
                        val cid = UUID.randomUUID().toString()
                        onTaskCorrelationIdChange(cid)
                        GlobalUIState.pendingTaskRequest.value = LinkedCreationData(
                            title = title, description = description, sourcePlugin = "calendar",
                            sourceItemId = initialEventId ?: "draft", correlationId = cid, causationId = null
                        )
                    } else {
                        onTaskCorrelationIdChange(null)
                    }
                }
            )
            
            Spacer(Modifier.width(32.dp))

            RemmiLinkedActionButton(
                icon = Icons.Default.Map,
                active = createLocation,
                onClick = { 
                    onCreateLocationChange(!createLocation)
                    if (!createLocation) {
                        val cid = UUID.randomUUID().toString()
                        onLocationCorrelationIdChange(cid)
                        GlobalUIState.showLocationPicker.value = true
                        GlobalUIState.locationPickerData.value = LinkedCreationData(
                            title = title, description = description, sourcePlugin = "calendar",
                            sourceItemId = initialEventId ?: "draft", correlationId = cid, causationId = null
                        )
                    } else {
                        onLocationCorrelationIdChange(null)
                    }
                }
            )

            Spacer(Modifier.width(32.dp))

            RemmiLinkedActionButton(
                icon = Icons.Default.Person,
                active = createContact,
                onClick = { onCreateContactChange(!createContact) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Participants", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            OutlinedCard(
                onClick = onShowParticipantsPopup,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GroupAdd, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (participants.isEmpty()) "Select Participants" else "${participants.size} participants selected", 
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun DateFieldRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onIconClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = date.dayOfMonth.toString(),
            onValueChange = { s -> s.toIntOrNull()?.let { if (it in 1..31) onDateChange(LocalDate(date.year, date.month, it)) } },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = date.monthNumber.toString(),
            onValueChange = { s -> s.toIntOrNull()?.let { if (it in 1..12) onDateChange(LocalDate(date.year, it, date.dayOfMonth)) } },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = date.year.toString(),
            onValueChange = { s -> s.toIntOrNull()?.let { onDateChange(LocalDate(it, date.month, date.dayOfMonth)) } },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        IconButton(onClick = onIconClick) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
        }
    }
}

@Composable
fun TimeFieldRow(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    onIconClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
            onValueChange = { s ->
                val parts = s.split(":")
                if (parts.size == 2) {
                    val h = parts[0].toIntOrNull() ?: 0
                    val m = parts[1].toIntOrNull() ?: 0
                    if (h in 0..23 && m in 0..59) onTimeChange(LocalTime(h, m))
                }
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        IconButton(onClick = onIconClick) {
            Icon(Icons.Default.Schedule, contentDescription = "Select Time")
        }
    }
}
