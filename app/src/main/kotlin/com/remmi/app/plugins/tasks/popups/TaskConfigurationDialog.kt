package com.remmi.app.plugins.tasks.popups

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.commands.CreateTaskCommand
import com.remmi.app.core.screens.components.RemmiPrioritySwitch
import com.remmi.app.core.screens.components.RemmiTitleDescriptionGroup
import com.remmi.app.core.controller.LinkedCreationData
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskConfigurationDialog(
    data: LinkedCreationData,
    onDismiss: () -> Unit,
    onConfirm: (CreateTaskCommand) -> Unit
) {
    Log.d("Remmi", "[TaskConfigurationDialog] - Displaying for ${data.sourcePlugin}")
    var title by remember { mutableStateOf(data.title) }
    var description by remember { mutableStateOf(data.description) }
    var isPriority by remember { mutableStateOf(false) }
    
    val timeZone = TimeZone.currentSystemDefault()
    val today = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(timeZone).date
    
    var dueDate by remember { mutableStateOf(today) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Linked Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RemmiTitleDescriptionGroup(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it }
                )
                
                RemmiPrioritySwitch(
                    isPriority = isPriority,
                    onPriorityChange = { isPriority = it },
                    label = "Priority Task"
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(
                    CreateTaskCommand(
                        title = title,
                        description = description,
                        dueDate = dueDate.atTime(0, 0).toInstant(timeZone),
                        isPriority = isPriority,
                        sourcePlugin = data.sourcePlugin,
                        sourceItemId = data.sourceItemId,
                        correlationId = data.correlationId,
                        causationId = data.causationId,
                        creationContext = CreationContext.SECONDARY_LINKED,
                        source = "tasks_plugin_ui"
                    )
                )
            }) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
