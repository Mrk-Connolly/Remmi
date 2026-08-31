package com.remmi.app.plugins.tasks.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.commands.CreateTaskCommand
import com.remmi.app.core.eventBus.commands.UpdateTaskCommand
import com.remmi.app.core.eventBus.commands.DeleteTaskCommand
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.core.plugin.model.components.RepeatType
import com.remmi.app.core.plugin.screens.RemmiAddScreen
import com.remmi.app.core.plugin.screens.RemmiUpdateScreen
import com.remmi.app.core.screens.components.*
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch
import androidx.compose.material3.MenuAnchorType

sealed class TaskEditorMode {
    data object Create : TaskEditorMode()
    data object Multitask : TaskEditorMode()
    data class Edit(val task: TaskItem) : TaskEditorMode()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksEditorScreen(
    mode: TaskEditorMode,
    actions: TasksActions,
    controller: RemmiController,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Log.d("Remmi", "[TasksEditorScreen] - Refactored")
    val scope = rememberCoroutineScope()
    val initialTask = (mode as? TaskEditorMode.Edit)?.task

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val initialDateTime = remember(initialTask) {
        initialTask?.dueDate?.toLocalDateTime(timeZone) ?: 
        Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(timeZone)
    }

    var startDate by remember { mutableStateOf(initialDateTime.date) }
    var startTime by remember { mutableStateOf(initialDateTime.time) }
    
    var isPriority by remember { mutableStateOf(initialTask?.isPriority ?: false) }
    var group by remember { mutableStateOf(initialTask?.group) }
    
    var isDueDateEnabled by remember { mutableStateOf(initialTask?.dueDate != null) }
    var isTimeEnabled by remember { mutableStateOf(initialTask?.dueDate != null) }

    var repeatType by remember { mutableStateOf(initialTask?.repeat?.type ?: RepeatType.NONE) }
    var repeatDays by remember { mutableStateOf(initialTask?.repeat?.days ?: emptyList()) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showRepeatDaysDialog by remember { mutableStateOf(false) }

    var existingGroups by remember { mutableStateOf(emptyList<String>()) }
    var isGroupExpanded by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        existingGroups = actions.getAllGroups()
    }
    
    var addToCalendar by remember { mutableStateOf(initialTask?.createCalendar ?: false) }
    var addToAlarm by remember { mutableStateOf(initialTask?.createAlarm ?: false) }

    val onSaveAction = {
        val finalDueDate = if (isDueDateEnabled) {
            try {
                val timeToUse = if (isTimeEnabled) startTime else LocalTime(23, 59)
                LocalDateTime(startDate.year, startDate.month, startDate.day, timeToUse.hour, timeToUse.minute).toInstant(timeZone)
            } catch (e: Exception) {
                initialTask?.dueDate
            }
        } else {
            null
        }
        val repeatRule = if (repeatType != RepeatType.NONE) {
            RepeatRule(repeatType, if (repeatType == RepeatType.CUSTOM) repeatDays else emptyList())
        } else {
            null
        }

        scope.launch {
            if (initialTask != null) {
                controller.eventBus.publishCommand(
                    UpdateTaskCommand(
                        task = initialTask.copy(
                            modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                            title = title,
                            description = description,
                            dueDate = finalDueDate,
                            isPriority = isPriority,
                            group = group,
                            repeat = repeatRule,
                            createCalendar = addToCalendar,
                            createAlarm = addToAlarm
                        )
                    )
                )
            } else {
                controller.eventBus.publishCommand(
                    CreateTaskCommand(
                        title = title,
                        description = description,
                        dueDate = finalDueDate,
                        isPriority = isPriority,
                        group = group,
                        repeat = repeatRule
                    )
                )
            }
            onSave()
        }
    }

    if (initialTask == null) {
        RemmiAddScreen(
            title = "New Task",
            onBack = onDismiss,
            onSave = { onSaveAction() },
            saveEnabled = title.isNotBlank()
        ) { padding ->
            EditorContent(
                padding = padding,
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                group = group, onGroupChange = { group = it },
                isGroupExpanded = isGroupExpanded, onGroupExpandedChange = { isGroupExpanded = it },
                existingGroups = existingGroups,
                onShowAddGroupDialog = { showAddGroupDialog = true },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                repeatType = repeatType, onRepeatTypeChange = { repeatType = it },
                repeatDays = repeatDays, onShowRepeatDaysDialog = { showRepeatDaysDialog = true },
                isDueDateEnabled = isDueDateEnabled, onIsDueDateEnabledChange = { isDueDateEnabled = it },
                isTimeEnabled = isTimeEnabled, onIsTimeEnabledChange = { isTimeEnabled = it },
                startDate = startDate, onShowStartDatePicker = { showStartDatePicker = true },
                startTime = startTime, onShowStartTimePicker = { showStartTimePicker = true },
                addToCalendar = addToCalendar, onAddToCalendarChange = { addToCalendar = it },
                addToAlarm = addToAlarm, onAddToAlarmChange = { addToAlarm = it },
                isEdit = false
            )
        }
    } else {
        RemmiUpdateScreen(
            title = "Edit Task",
            onBack = onDismiss,
            onDelete = {
                scope.launch {
                    controller.eventBus.publishCommand(DeleteTaskCommand(taskId = initialTask.id))
                    onSave()
                }
            },
            onSave = { onSaveAction() },
            saveEnabled = title.isNotBlank()
        ) { padding ->
            EditorContent(
                padding = padding,
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it },
                group = group, onGroupChange = { group = it },
                isGroupExpanded = isGroupExpanded, onGroupExpandedChange = { isGroupExpanded = it },
                existingGroups = existingGroups,
                onShowAddGroupDialog = { showAddGroupDialog = true },
                isPriority = isPriority, onIsPriorityChange = { isPriority = it },
                repeatType = repeatType, onRepeatTypeChange = { repeatType = it },
                repeatDays = repeatDays, onShowRepeatDaysDialog = { showRepeatDaysDialog = true },
                isDueDateEnabled = isDueDateEnabled, onIsDueDateEnabledChange = { isDueDateEnabled = it },
                isTimeEnabled = isTimeEnabled, onIsTimeEnabledChange = { isTimeEnabled = it },
                startDate = startDate, onShowStartDatePicker = { showStartDatePicker = true },
                startTime = startTime, onShowStartTimePicker = { showStartTimePicker = true },
                addToCalendar = addToCalendar, onAddToCalendarChange = { addToCalendar = it },
                addToAlarm = addToAlarm, onAddToAlarmChange = { addToAlarm = it },
                isEdit = true
            )
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

    if (showStartDatePicker) {
        RemmiDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { newDate ->
                startDate = newDate
            }
        )
    }

    if (showRepeatDaysDialog) {
        RemmiDaySelectionDialog(
            selectedDays = repeatDays.map { it.name },
            onDismiss = { showRepeatDaysDialog = false },
            onConfirm = { days ->
                repeatDays = days.map { DayOfWeek.valueOf(it) }
                showRepeatDaysDialog = false
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorContent(
    padding: PaddingValues,
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    group: String?, onGroupChange: (String?) -> Unit,
    isGroupExpanded: Boolean, onGroupExpandedChange: (Boolean) -> Unit,
    existingGroups: List<String>,
    onShowAddGroupDialog: () -> Unit,
    isPriority: Boolean, onIsPriorityChange: (Boolean) -> Unit,
    repeatType: RepeatType, onRepeatTypeChange: (RepeatType) -> Unit,
    repeatDays: List<DayOfWeek>, onShowRepeatDaysDialog: () -> Unit,
    isDueDateEnabled: Boolean, onIsDueDateEnabledChange: (Boolean) -> Unit,
    isTimeEnabled: Boolean, onIsTimeEnabledChange: (Boolean) -> Unit,
    startDate: LocalDate, onShowStartDatePicker: () -> Unit,
    startTime: LocalTime, onShowStartTimePicker: () -> Unit,
    addToCalendar: Boolean, onAddToCalendarChange: (Boolean) -> Unit,
    addToAlarm: Boolean, onAddToAlarmChange: (Boolean) -> Unit,
    isEdit: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        RemmiTitleDescriptionGroup(
            title = title,
            onTitleChange = onTitleChange,
            description = description,
            onDescriptionChange = onDescriptionChange
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = isGroupExpanded,
                onExpandedChange = onGroupExpandedChange,
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
                    onDismissRequest = { onGroupExpandedChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("No Group") },
                        onClick = { onGroupChange(null); onGroupExpandedChange(false) }
                    )
                    existingGroups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = { onGroupChange(g); onGroupExpandedChange(false) }
                        )
                    }
                }
            }
            IconButton(onClick = onShowAddGroupDialog) {
                Icon(Icons.Default.Add, contentDescription = "New Group")
            }
        }

        RemmiPrioritySwitch(
            isPriority = isPriority,
            onPriorityChange = onIsPriorityChange,
            label = "Priority Task"
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // --- Repeatable Section ---
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Repeat Interval", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                val types = listOf(RepeatType.NONE, RepeatType.DAILY, RepeatType.WEEKLY, RepeatType.MONTHLY, RepeatType.YEARLY, RepeatType.CUSTOM)
                types.chunked(3).forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowTypes.forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = repeatType == type,
                                    onClick = { 
                                        onRepeatTypeChange(type)
                                        if (type == RepeatType.CUSTOM) onShowRepeatDaysDialog()
                                    }
                                )
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                if (repeatType == RepeatType.CUSTOM) {
                    Text(
                        text = "Selected: ${repeatDays.joinToString { it.name.lowercase().take(3).replaceFirstChar { it.uppercase() } }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onShowRepeatDaysDialog() }.padding(vertical = 4.dp)
                    )
                }
            }

            // --- Due Date Section ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Due Date & Time", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDueDateEnabled, onCheckedChange = onIsDueDateEnabledChange)
                    OutlinedButton(
                        onClick = onShowStartDatePicker,
                        modifier = Modifier.weight(1f),
                        enabled = isDueDateEnabled,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(if (isDueDateEnabled) startDate.toString() else "Set Date")
                    }
                    
                    Checkbox(checked = isTimeEnabled, onCheckedChange = onIsTimeEnabledChange, enabled = isDueDateEnabled)
                    OutlinedButton(
                        onClick = onShowStartTimePicker,
                        modifier = Modifier.weight(1f),
                        enabled = isDueDateEnabled && isTimeEnabled,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Text(if (isDueDateEnabled && isTimeEnabled) startTime.toString().substring(0, 5) else "Set Time")
                    }
                }
            }

            // --- Action Options ---
            if (!isEdit) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RemmiLinkedActionButton(
                        icon = Icons.Default.CalendarMonth,
                        active = addToCalendar,
                        onClick = { onAddToCalendarChange(!addToCalendar) }
                    )
                    
                    Spacer(Modifier.width(32.dp))

                    RemmiLinkedActionButton(
                        icon = Icons.Default.Alarm,
                        active = addToAlarm,
                        onClick = { onAddToAlarmChange(!addToAlarm) }
                    )
                }
            }
        }
    }
}
