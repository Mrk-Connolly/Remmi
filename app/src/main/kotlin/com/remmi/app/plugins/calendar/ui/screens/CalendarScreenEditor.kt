package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.screens.components.RemmiDatePickerDialog
import com.remmi.app.core.screens.components.RemmiTimePickerDialog
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarItem
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
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
    var startingDate by remember { mutableStateOf(initialEvent?.startingDate ?: today) }
    var startingTime by remember { mutableStateOf(initialEvent?.startingTime) }
    var endingDate by remember { mutableStateOf(initialEvent?.endingDate ?: startingDate) }
    var endingTime by remember { mutableStateOf(initialEvent?.endingTime) }
    var isPriority by remember { mutableStateOf(initialEvent?.isPriority ?: false) }
    var group by remember { mutableStateOf(initialEvent?.group ?: "") }
    
    // Linked Creation Flags
    var createAlarm by remember { mutableStateOf(initialEvent?.createAlarm ?: false) }
    var createTask by remember { mutableStateOf(initialEvent?.createTask ?: false) }
    var createLocation by remember { mutableStateOf(initialEvent?.createLocation ?: false) }
    var createContact by remember { mutableStateOf(initialEvent?.createContact ?: false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialEvent == null) "Create Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (initialEvent == null) {
                                    actions.addEvent(
                                        title = title,
                                        description = description,
                                        startingDate = startingDate,
                                        startingTime = startingTime,
                                        endingDate = endingDate,
                                        endingTime = endingTime,
                                        isPriority = isPriority,
                                        group = if (group.isEmpty()) null else group,
                                        createAlarm = createAlarm,
                                        createTask = createTask,
                                        createLocation = createLocation,
                                        createContact = createContact
                                    )
                                } else {
                                    actions.updateEvent(
                                        initialEvent.copy(
                                            title = title,
                                            description = description,
                                            startingDate = startingDate,
                                            startingTime = startingTime,
                                            endingDate = endingDate,
                                            endingTime = endingTime,
                                            isPriority = isPriority,
                                            group = if (group.isEmpty()) null else group,
                                            createAlarm = createAlarm,
                                            createTask = createTask,
                                            createLocation = createLocation,
                                            createContact = createContact
                                        )
                                    )
                                }
                                onSave()
                            }
                        },
                        enabled = title.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = group,
                onValueChange = { group = it },
                label = { Text("Group") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start: $startingDate")
                }
                Button(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(startingTime?.toString() ?: "No Time")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End: $endingDate")
                }
                Button(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(endingTime?.toString() ?: "No Time")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                Text("Priority Event")
            }

            HorizontalDivider()
            Text("Linked Actions", style = MaterialTheme.typography.titleSmall)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
        }
    }

    if (showStartDatePicker) {
        RemmiDatePickerDialog(
            initialDate = startingDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { startingDate = it }
        )
    }
    if (showStartTimePicker) {
        RemmiTimePickerDialog(
            initialTime = startingTime ?: LocalTime(9, 0),
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { startingTime = it }
        )
    }
    if (showEndDatePicker) {
        RemmiDatePickerDialog(
            initialDate = endingDate,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { endingDate = it }
        )
    }
    if (showEndTimePicker) {
        RemmiTimePickerDialog(
            initialTime = endingTime ?: LocalTime(10, 0),
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = { endingTime = it }
        )
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
