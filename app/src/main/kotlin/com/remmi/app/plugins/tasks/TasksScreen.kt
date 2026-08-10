package com.remmi.app.plugins.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Main screen for the Tasks plugin.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(actions: TasksActions) {
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var editorMode by remember { mutableStateOf<TaskEditorMode?>(null) }
    var taskToManage by remember { mutableStateOf<TaskItem?>(null) }

    // Refresh tasks on load
    LaunchedEffect(Unit) {
        tasks = actions.getAllTasks()
    }

    if (editorMode != null) {
        TasksEditorScreen(
            mode = editorMode!!,
            actions = actions,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    tasks = actions.getAllTasks()
                    editorMode = null
                }
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { editorMode = TaskEditorMode.Create }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { padding ->
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekEnd = today.plus(7, DateTimeUnit.DAY)

            val noDateTasks = tasks.filter { it.dueDate == null }
            val todayTasks = tasks.filter { 
                it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today 
            }
            val weekTasks = tasks.filter { 
                val date = it.dueDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
                date != null && date > today && date <= weekEnd
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                if (tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tasks yet. Tap + to add one.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (noDateTasks.isNotEmpty()) {
                            item { TaskSectionHeader("No Date") }
                            items(noDateTasks, key = { it.id }) { task ->
                                TaskRow(task, actions, onUpdate = { tasks = it }, onLongClick = { taskToManage = task })
                            }
                        }

                        if (todayTasks.isNotEmpty()) {
                            item { TaskSectionHeader("Today") }
                            items(todayTasks, key = { it.id }) { task ->
                                TaskRow(task, actions, onUpdate = { tasks = it }, onLongClick = { taskToManage = task })
                            }
                        }

                        if (weekTasks.isNotEmpty()) {
                            item { TaskSectionHeader("This Week") }
                            items(weekTasks, key = { it.id }) { task ->
                                TaskRow(task, actions, onUpdate = { tasks = it }, onLongClick = { taskToManage = task })
                            }
                        }
                    }
                }
            }
        }
    }

    // Management Popup (Edit/Delete)
    if (taskToManage != null) {
        AlertDialog(
            onDismissRequest = { taskToManage = null },
            title = { Text("Manage Task") },
            text = { Text("Choose an action for \"${taskToManage!!.title}\"") },
            confirmButton = {
                TextButton(onClick = {
                    editorMode = TaskEditorMode.Edit(taskToManage!!)
                    taskToManage = null
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            actions.deleteTask(taskToManage!!.id)
                            tasks = actions.getAllTasks()
                            taskToManage = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        )
    }
}

@Composable
fun TaskSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: TaskItem,
    actions: TasksActions,
    onUpdate: (List<TaskItem>) -> Unit,
    onLongClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isCompleted by remember(task.id, task.completed) { mutableStateOf(task.completed) }
    val priorityColor = getPriorityColor(task.priority)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = priorityColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { checked ->
                    isCompleted = checked
                    scope.launch {
                        actions.toggleTask(task)
                        onUpdate(actions.getAllTasks())
                    }
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            if (task.priority != Priority.Normal) {
                Text(
                    text = task.priority.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (task.priority) {
                        Priority.High -> Color(0xFFD32F2F)
                        Priority.Low -> Color(0xFF388E3C)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.High -> Color(0xFFE57373)   // Red
        Priority.Normal -> Color(0xFFFFD54F) // Yellow
        Priority.Low -> Color(0xFF81C784)    // Green
    }
}
