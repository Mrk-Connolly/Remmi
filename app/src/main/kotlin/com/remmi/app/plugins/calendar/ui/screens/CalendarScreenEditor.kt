package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.calendar.ui.popups.NewGroupDialog
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
    var startingDate by remember { mutableStateOf(initialEvent?.startingDate ?: today) }
    var startingTime by remember { mutableStateOf(initialEvent?.startingTime ?: LocalTime(9, 0)) }
    var endingDate by remember { mutableStateOf(initialEvent?.endingDate ?: startingDate) }
    var endingTime by remember { mutableStateOf(initialEvent?.endingTime ?: LocalTime(10, 0)) }
    
    // Repeat State
    var isRepeatable by remember { mutableStateOf(initialEvent?.isRepeatable ?: false) }
    var repeatableType by remember { mutableStateOf(initialEvent?.repeatableType ?: "None") }

    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    
    // Linked Creation Flags
    var createAlarm by remember { mutableStateOf(initialEvent?.createAlarm ?: false) }
    var createTask by remember { mutableStateOf(initialEvent?.createTask ?: false) }
    var createLocation by remember { mutableStateOf(initialEvent?.createLocation ?: false) }
    var createContact by remember { mutableStateOf(initialEvent?.createContact ?: false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            groups = actions.getCalendarGroups()
            Log.d("Remmi", "[CalendarScreenEditor] - Groups fetched: ${groups.size}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialEvent == null) "New Event" else "Edit Event", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                                val item = if (initialEvent == null) {
                                    CalendarItem(
                                        id = UUID.randomUUID().toString(),
                                        created = now,
                                        modified = now,
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
                                } else {
                                    initialEvent.copy(
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
                                }
                                
                                if (initialEvent == null) {
                                    actions.addEvent(
                                        title = item.title,
                                        description = item.description,
                                        startingDate = item.startingDate,
                                        startingTime = item.startingTime,
                                        endingDate = item.endingDate,
                                        endingTime = item.endingTime,
                                        isPriority = item.isPriority,
                                        group = item.group,
                                        isRepeatable = item.isRepeatable,
                                        repeatableType = item.repeatableType,
                                        createAlarm = item.createAlarm,
                                        createTask = item.createTask,
                                        createLocation = item.createLocation,
                                        createContact = item.createContact
                                    )
                                } else {
                                    actions.updateEvent(item)
                                }
                                onSave()
                            }
                        },
                        enabled = title.isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Description (New)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

    // Group Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Group", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { 
                                scope.launch {
                                    groups = actions.getCalendarGroups()
                                    expanded = true 
                                }
                            },
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
                                onClick = { groupName = ""; expanded = false }
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
                                    onClick = { groupName = g.name; expanded = false }
                                )
                            }
                        }
                    }
                    
                    // Add Group Button
                    FilledIconButton(
                        onClick = { showNewGroupDialog = true },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Group", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Start Date/Time
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Start", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                DateFieldRow(
                    date = startingDate,
                    onDateChange = { startingDate = it },
                    onIconClick = { showStartDatePicker = true }
                )
                TimeFieldRow(
                    time = startingTime,
                    onTimeChange = { startingTime = it },
                    onIconClick = { showStartTimePicker = true }
                )
            }

            // End Date/Time
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("End", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                DateFieldRow(
                    date = endingDate,
                    onDateChange = { endingDate = it },
                    onIconClick = { showEndDatePicker = true }
                )
                TimeFieldRow(
                    time = endingTime,
                    onTimeChange = { endingTime = it },
                    onIconClick = { showEndTimePicker = true }
                )
            }

            // Repeatable Options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRepeatable, onCheckedChange = { isRepeatable = it })
                    Text("Repeat Event", fontWeight = FontWeight.Bold)
                }
                if (isRepeatable) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Daily", "Weekly", "Monthly", "Yearly").forEach { type ->
                            FilterChip(
                                selected = repeatableType == type,
                                onClick = { repeatableType = type },
                                label = { Text(type) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Priority
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                Text("Priority Event", fontWeight = FontWeight.Bold)
            }

            // Linked Actions (No title or divider)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), 
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LinkedActionButton(
                    icon = Icons.Default.Alarm,
                    active = createAlarm,
                    onClick = { 
                        createAlarm = !createAlarm
                        if (createAlarm) {
                            com.remmi.app.core.controller.GlobalUIState.pendingAlarmRequest.value = com.remmi.app.core.controller.LinkedCreationData(
                                title = title,
                                description = description,
                                sourcePlugin = "calendar",
                                sourceItemId = initialEvent?.id ?: "draft",
                                correlationId = UUID.randomUUID().toString(),
                                causationId = null
                            )
                        }
                    }
                )
                LinkedActionButton(
                    icon = Icons.Default.CheckCircle,
                    active = createTask,
                    onClick = { 
                        createTask = !createTask
                        if (createTask) {
                            com.remmi.app.core.controller.GlobalUIState.pendingTaskRequest.value = com.remmi.app.core.controller.LinkedCreationData(
                                title = title,
                                description = description,
                                sourcePlugin = "calendar",
                                sourceItemId = initialEvent?.id ?: "draft",
                                correlationId = UUID.randomUUID().toString(),
                                causationId = null
                            )
                        }
                    }
                )
                LinkedActionButton(
                    icon = Icons.Default.Map,
                    active = createLocation,
                    onClick = { 
                        createLocation = !createLocation
                        if (createLocation) {
                            com.remmi.app.core.controller.GlobalUIState.showLocationPicker.value = true
                            com.remmi.app.core.controller.GlobalUIState.locationPickerData.value = com.remmi.app.core.controller.LinkedCreationData(
                                title = title,
                                description = description,
                                sourcePlugin = "calendar",
                                sourceItemId = initialEvent?.id ?: "draft",
                                correlationId = UUID.randomUUID().toString(),
                                causationId = null
                            )
                        }
                    }
                )
                LinkedActionButton(
                    icon = Icons.Default.Person,
                    active = createContact,
                    onClick = { createContact = !createContact }
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
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

@Composable
fun DateFieldRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onIconClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Day
        OutlinedTextField(
            value = date.dayOfMonth.toString(),
            onValueChange = { s -> s.toIntOrNull()?.let { if (it in 1..31) onDateChange(LocalDate(date.year, date.month, it)) } },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        // Month
        OutlinedTextField(
            value = date.monthNumber.toString(),
            onValueChange = { s -> s.toIntOrNull()?.let { if (it in 1..12) onDateChange(LocalDate(date.year, it, date.dayOfMonth)) } },
            modifier = Modifier.width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
        // Year
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

@Composable
fun LinkedActionButton(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(icon, contentDescription = null, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
