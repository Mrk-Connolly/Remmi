package com.remmi.app.plugins.tasks.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.commands.CreateTaskCommand
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTitleDescriptionGroup
import com.remmi.app.core.controller.LinkedCreationData
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

data class TaskDraft(
    val title: String,
    val description: String,
    val isPriority: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskConfigurationDialog(
    data: LinkedCreationData,
    onDismiss: () -> Unit,
    onConfirm: (CreateTaskCommand) -> Unit
) {
    Log.d("Remmi", "[TaskConfigurationDialog] - Displaying for ${data.sourcePlugin}")
    
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(timeZone).date }
    
    var currentTitle by remember { mutableStateOf(data.title) }
    var currentDescription by remember { mutableStateOf(data.description) }
    var currentIsPriority by remember { mutableStateOf(false) }
    
    val taskList = remember { mutableStateListOf<TaskDraft>() }
    var isAddingTask by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Linked Tasks") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (isAddingTask) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RemmiTitleDescriptionGroup(
                            title = currentTitle,
                            onTitleChange = { currentTitle = it },
                            description = currentDescription,
                            onDescriptionChange = { currentDescription = it }
                        )
                        
                        RemmiPrioritySwitch(
                            isPriority = currentIsPriority,
                            onPriorityChange = { currentIsPriority = it },
                            label = "Priority Task"
                        )
                        
                        Button(
                            onClick = {
                                if (currentTitle.isNotBlank()) {
                                    taskList.add(TaskDraft(currentTitle, currentDescription, currentIsPriority))
                                    currentTitle = ""
                                    currentDescription = ""
                                    currentIsPriority = false
                                    isAddingTask = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Task to List")
                        }
                    }
                } else {
                    Column {
                        Text("Tasks to Create:", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            items(taskList) { task ->
                                ListItem(
                                    headlineContent = { Text(task.title) },
                                    supportingContent = { if (task.description.isNotEmpty()) Text(task.description) },
                                    trailingContent = {
                                        IconButton(onClick = { taskList.remove(task) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                                        }
                                    }
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = { isAddingTask = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add Another Task")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // Add current draft if not empty
                    if (isAddingTask && currentTitle.isNotBlank()) {
                        taskList.add(TaskDraft(currentTitle, currentDescription, currentIsPriority))
                    }
                    
                    taskList.forEach { task ->
                        onConfirm(
                            CreateTaskCommand(
                                title = task.title,
                                description = task.description,
                                dueDate = today.atTime(0, 0).toInstant(timeZone),
                                isPriority = task.isPriority,
                                sourcePlugin = data.sourcePlugin,
                                sourceItemId = data.sourceItemId,
                                correlationId = data.correlationId,
                                causationId = data.causationId,
                                creationContext = CreationContext.SECONDARY_LINKED,
                                source = "tasks_plugin_ui"
                            )
                        )
                    }
                    onDismiss()
                },
                enabled = taskList.isNotEmpty() || (isAddingTask && currentTitle.isNotBlank())
            ) {
                Text("Create Tasks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
