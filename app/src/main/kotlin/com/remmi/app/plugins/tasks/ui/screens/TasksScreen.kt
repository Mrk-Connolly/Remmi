package com.remmi.app.plugins.tasks.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.DeleteTaskCommand
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.TaskItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Main screen for the Tasks plugin.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    actions: TasksActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[TasksScreen] - [TasksScreen] executed")
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var editorMode by remember { mutableStateOf<TaskEditorMode?>(null) }
    
    // Track editor state for hiding bottom menu
    LaunchedEffect(editorMode) {
        controller.isEditorActive.value = editorMode != null
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.isEditorActive.value = false
        }
    }

    var taskToManage by remember { mutableStateOf<TaskItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedGroupFilter by remember { mutableStateOf("All") }
    var onlyImportant by remember { mutableStateOf(false) }
    var existingGroups by remember { mutableStateOf(emptyList<String>()) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                tasks = actions.getAllTasks()
                existingGroups = actions.getAllGroups()
                delay(500)
                isRefreshing = false
            }
        }
    }

    // Refresh tasks on load
    LaunchedEffect(Unit) {
        tasks = actions.getAllTasks()
        existingGroups = actions.getAllGroups()
    }

    val filteredTasks = remember(tasks, selectedGroupFilter, onlyImportant) {
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val baseFiltered = if (selectedGroupFilter == "All") tasks
        else tasks.filter { it.group == selectedGroupFilter }
        
        if (onlyImportant) {
            baseFiltered.filter { it.isPriority || (it.dueDate != null && !it.completed && it.dueDate < now) }
        } else {
            baseFiltered
        }
    }

    if (editorMode != null) {
        TasksEditorScreen(
            mode = editorMode!!,
            actions = actions,
            controller = controller,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    tasks = actions.getAllTasks()
                    existingGroups = actions.getAllGroups()
                    editorMode = null
                }
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { editorMode = TaskEditorMode.Create },
                    modifier = Modifier.padding(bottom = 176.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { padding ->
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            val taskSections = remember(filteredTasks) {
                val ongoing = filteredTasks.filter { it.dueDate == null }.sortedByDescending { it.created }
                val daily = filteredTasks.filter { 
                    it.dueDate != null && 
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
                }.sortedBy { it.dueDate }
                val upcoming = filteredTasks.filter { 
                    it.dueDate != null && 
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date > today 
                }.sortedBy { it.dueDate }
                
                listOf(
                    "Ongoing" to ongoing,
                    "Daily" to daily,
                    "Upcoming" to upcoming
                ).filter { it.second.isNotEmpty() }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Group Filter Dropdown
                    var isFilterExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onlyImportant = !onlyImportant },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (onlyImportant) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Filter Important",
                                tint = if (onlyImportant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            TextButton(
                                onClick = { isFilterExpanded = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.FilterList, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Filter: $selectedGroupFilter")
                            }
                            DropdownMenu(
                                expanded = isFilterExpanded,
                                onDismissRequest = { isFilterExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All") },
                                    onClick = {
                                        selectedGroupFilter = "All"
                                        isFilterExpanded = false
                                    }
                                )
                                existingGroups.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        onClick = {
                                            selectedGroupFilter = g
                                            isFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (filteredTasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tasks found.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 180.dp)
                        ) {
                            taskSections.forEach { (sectionName, tasksInSection) ->
                                item { TaskSectionHeader(sectionName) }
                                items(tasksInSection, key = { it.id }) { task ->
                                    TaskRow(task, actions, onUpdate = { tasks = it }, onLongClick = { taskToManage = task })
                                }
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
                            controller.eventBus.publishCommand(
                                DeleteTaskCommand(taskId = taskToManage!!.id)
                            )
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
    Log.d("Remmi", "[TasksScreen] - [TaskSectionHeader] executed")
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
    Log.d("Remmi", "[TasksScreen] - [TaskRow] executed")
    val scope = rememberCoroutineScope()
    var isCompleted by remember(task.id, task.completed) { mutableStateOf(task.completed) }
    var isExpanded by remember { mutableStateOf(false) }
    
    val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
    val isOverdue = !task.completed && task.dueDate != null && task.dueDate < now
    
    val cardColor = if (task.isPriority || isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (task.isPriority || isOverdue) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                        fontWeight = if (task.isPriority || isOverdue) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (task.isPriority || isOverdue) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Priority",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (isOverdue && task.dueDate != null) {
                    val dueDateStr = task.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    Text(
                        text = "Overdue: $dueDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded && task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                onClick = {
                    isCompleted = !isCompleted
                    scope.launch {
                        actions.toggleTask(task)
                        onUpdate(actions.getAllTasks())
                    }
                },
                shape = CircleShape,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = if (isCompleted) null else BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(24.dp)
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
