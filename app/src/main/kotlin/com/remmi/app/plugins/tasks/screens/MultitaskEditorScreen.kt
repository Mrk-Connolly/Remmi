package com.remmi.app.plugins.tasks.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.core.plugin.model.components.RepeatType
import com.remmi.app.core.plugin.screens.RemmiAddScreen
import com.remmi.app.core.screens.components.*
import com.remmi.app.plugins.tasks.TasksActions
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultitaskEditorScreen(
    actions: TasksActions,
    controller: RemmiController,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var titles by remember { mutableStateOf(listOf("")) }
    var description by remember { mutableStateOf("") }
    var group by remember { mutableStateOf<String?>(null) }
    var subgroup by remember { mutableStateOf("") }
    
    var startDate by remember { mutableStateOf(java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }) }
    var startTime by remember { mutableStateOf(LocalTime(23, 59)) }
    
    var isPriority by remember { mutableStateOf(false) }
    var isDueDateEnabled by remember { mutableStateOf(false) }
    var isTimeEnabled by remember { mutableStateOf(false) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }

    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var addToCalendar by remember { mutableStateOf(false) }
    var addToAlarm by remember { mutableStateOf(false) }

    RemmiAddScreen(
        title = "New Multitask",
        onBack = onDismiss,
        onSave = {
            scope.launch {
                val finalDueDate = if (isDueDateEnabled) {
                    val timeToUse = if (isTimeEnabled) startTime else LocalTime(23, 59)
                    LocalDateTime(startDate.year, startDate.month, startDate.day, timeToUse.hour, timeToUse.minute)
                        .toInstant(TimeZone.currentSystemDefault())
                } else null
                
                actions.createMultitask(
                    titles = titles,
                    description = description,
                    group = group,
                    subgroup = subgroup.takeIf { it.isNotBlank() },
                    dueDate = finalDueDate,
                    isPriority = isPriority,
                    repeat = if (repeatType != RepeatType.NONE) RepeatRule(repeatType) else null,
                    createAlarm = addToAlarm,
                    createCalendar = addToCalendar
                )
                onSave()
            }
        },
        saveEnabled = titles.any { it.isNotBlank() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = subgroup,
                onValueChange = { subgroup = it },
                label = { Text("Project / Subgroup Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Tasks in this project", style = MaterialTheme.typography.titleMedium)
            
            titles.forEachIndexed { index, title ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { new ->
                            val newList = titles.toMutableList()
                            newList[index] = new
                            titles = newList
                        },
                        placeholder = { Text("Task title") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = {
                        if (titles.size > 1) {
                            titles = titles.toMutableList().apply { removeAt(index) }
                        }
                    }) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            TextButton(
                onClick = { titles = titles + "" },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add another task")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            RemmiPrioritySwitch(isPriority = isPriority, onPriorityChange = { isPriority = it })
            
            // Shared Date/Time Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDueDateEnabled, onCheckedChange = { isDueDateEnabled = it })
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f),
                        enabled = isDueDateEnabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isDueDateEnabled) startDate.toString() else "Set Date")
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isTimeEnabled, onCheckedChange = { isTimeEnabled = it }, enabled = isDueDateEnabled)
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f),
                        enabled = isDueDateEnabled && isTimeEnabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isDueDateEnabled && isTimeEnabled) startTime.toString().substring(0, 5) else "Set Time")
                    }
                }
            }

            // Quick Actions (Matching Calendar style)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemmiLinkedActionButton(
                    icon = Icons.Default.CalendarMonth,
                    active = addToCalendar,
                    onClick = { addToCalendar = !addToCalendar }
                )
                
                Spacer(Modifier.width(32.dp))

                RemmiLinkedActionButton(
                    icon = Icons.Default.Alarm,
                    active = addToAlarm,
                    onClick = { addToAlarm = !addToAlarm }
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showStartDatePicker) {
        RemmiDatePickerDialog(
            initialDate = startDate,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { startDate = it }
        )
    }

    if (showStartTimePicker) {
        RemmiTimePickerDialog(
            initialTime = startTime,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { startTime = it }
        )
    }
}
