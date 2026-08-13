package com.remmi.app.plugins.tasks

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Main screen for the Tasks plugin.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(actions: TasksActions) {
    Log.d("Remmi", "[TasksScreen] - [TasksScreen] executed")
    val scope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var editorMode by remember { mutableStateOf<TaskEditorMode?>(null) }
    var taskToManage by remember { mutableStateOf<TaskItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedGroupFilter by remember { mutableStateOf("All") }
    var existingGroups by remember { mutableStateOf(emptyList<String>()) }

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            tasks = actions.getAllTasks()
            existingGroups = actions.getAllGroups()
            delay(500)
            isRefreshing = false
        }
    }

    // Refresh tasks on load
    LaunchedEffect(Unit) {
        tasks = actions.getAllTasks()
        existingGroups = actions.getAllGroups()
    }

    val filteredTasks = remember(tasks, selectedGroupFilter) {
        if (selectedGroupFilter == "All") tasks
        else tasks.filter { it.group == selectedGroupFilter }
    }

    if (editorMode != null) {
        TasksEditorScreen(
            mode = editorMode!!,
            actions = actions,
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
                FloatingActionButton(onClick = { editorMode = TaskEditorMode.Create }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            },
            bottomBar = {
                Spacer(Modifier.height(96.dp))
            }
        ) { padding ->
            val groupedByGroup = filteredTasks.groupBy { it.group ?: "No Group" }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
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
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            groupedByGroup.forEach { (groupName, tasksInGroup) ->
                                item { TaskSectionHeader(groupName) }
                                items(tasksInGroup, key = { it.id }) { task ->
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
    val cardColor = if (task.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (task.isPriority) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
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
                        fontWeight = if (task.isPriority) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (task.isPriority) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Priority",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
