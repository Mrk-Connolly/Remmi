package com.remmi.app.plugins.tasks.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.DesignTokens
import com.remmi.app.core.eventBus.commands.DeleteTaskCommand
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.RemmiSectionHeader
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.plugins.tasks.models.SubTask
import com.remmi.app.ui.components.getIconForName
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
    
    // Reactive data from repository
    val tasksFlow = remember { actions.repository.asFlow() }
    val tasks by tasksFlow.collectAsState(initial = actions.repository.getAll())
    
    var editorMode by remember { mutableStateOf<TaskEditorMode?>(null) }
    
    var taskToManage by remember { mutableStateOf<TaskItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    
    // Compute groups reactively from tasks
    val existingGroups = remember(tasks) {
        tasks.mapNotNull { it.group }.distinct().sorted()
    }
    val tabs = remember(existingGroups) { listOf("Main") + existingGroups }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                actions.sync()
                delay(500)
                isRefreshing = false
            }
        }
    }

    val filteredTasks = remember(tasks, selectedTabIndex, tabs) {
        val selectedGroup = tabs.getOrNull(selectedTabIndex) ?: "Main"
        val sortedTasks = tasks.sortedByDescending { it.created }
        if (selectedGroup == "Main") sortedTasks
        else sortedTasks.filter { it.group == selectedGroup }
    }

    if (editorMode != null) {
        TasksEditorScreen(
            mode = editorMode!!,
            actions = actions,
            controller = controller,
            onDismiss = { editorMode = null },
            onSave = {
                editorMode = null
            }
        )
    } else {
        RemmiHomeScreen(
            title = "Tasks",
            floatingActionButton = {
                RemmiFAB(
                    onClick = { editorMode = TaskEditorMode.Create },
                    icon = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        ) { padding ->
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            val taskSections = remember(filteredTasks) {
                val currentNow = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                val currentToday = currentNow.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val endOfWeek = currentToday.plus(7, DateTimeUnit.DAY)

                val daily = filteredTasks.filter { 
                    !it.completed && it.dueDate != null &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date == currentToday 
                }.sortedBy { it.dueDate }
                
                val weekly = filteredTasks.filter { 
                    !it.completed && it.dueDate != null &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date > currentToday &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date <= endOfWeek
                }.sortedBy { it.dueDate }
                
                val later = filteredTasks.filter {
                    !it.completed && (it.dueDate == null || it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date > endOfWeek)
                }.sortedByDescending { it.created }
                
                val completed = filteredTasks.filter { it.completed }.sortedByDescending { it.completedAt ?: it.modified }
                
                listOf(
                    "Daily" to daily,
                    "Weekly" to weekly,
                    "Later / No Date" to later,
                    "Finished" to completed
                ).filter { it.second.isNotEmpty() }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Handles TopAppBar padding
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // --- Scrollable Tab Row ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecondaryScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            edgePadding = 8.dp,
                            containerColor = Color.Transparent,
                            divider = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { showAddGroupDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Group", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (filteredTasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tasks found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = DesignTokens.SpacingLarge,
                                end = DesignTokens.SpacingLarge,
                                top = DesignTokens.SpacingMedium,
                                bottom = 100.dp
                            )
                        ) {
                            taskSections.forEach { (sectionName, tasksInSection) ->
                                item {
                                    RemmiSectionHeader(
                                        title = sectionName,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(tasksInSection, key = { it.id }) { task ->
                                    TaskRow(
                                        task = task,
                                        actions = actions,
                                        onLongClick = { taskToManage = task }
                                    )
                                    Spacer(Modifier.height(DesignTokens.SpacingMedium))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("New Group") },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            // In a reactive system, we'd ideally create a task with this group
                            // to make it appear, or maintain a separate 'empty groups' state.
                            // For now, let's just close the dialog.
                            showAddGroupDialog = false
                            newGroupName = ""
                        }
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) { Text("Cancel") }
            }
        )
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskRow(
    task: TaskItem,
    actions: TasksActions,
    onLongClick: () -> Unit
) {
    Log.d("Remmi", "[TasksScreen] - [TaskRow] executed")
    val scope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var showFinishedSubtasks by remember { mutableStateOf(false) }
    
    val ongoingSubtasks = task.subTasks.filter { !it.completed }
    val finishedSubtasks = task.subTasks.filter { it.completed }

    val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
    val isOverdue = !task.completed && task.dueDate != null && task.dueDate < now
    
    val cardColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                    else if (task.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface

    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { isExpanded = !isExpanded },
                onLongClick = onLongClick
            ),
        containerColor = cardColor
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // --- Parent Task Header ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                    )

                    if (task.dueDate != null) {
                        val dueDateStr = task.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        Text(
                            text = if (isOverdue) "Overdue: $dueDateStr" else "Due: $dueDateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Completion Checkbox
                Surface(
                    onClick = {
                        scope.launch {
                            actions.toggleTask(task)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (task.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
                    border = BorderStroke(
                        2.dp,
                        if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    if (task.completed) {
                        Icon(Icons.Default.Check, "Completed", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(6.dp))
                    }
                }
            }

            // Description (only if expanded)
            AnimatedVisibility(visible = isExpanded && task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // --- Sub-tasks Section ---
            if (task.subTasks.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                // Ongoing Sub-tasks
                ongoingSubtasks.forEach { subTask ->
                    SubTaskItem(subTask, task, actions)
                    Spacer(Modifier.height(8.dp))
                }

                // Finished Sub-tasks Toggle
                if (finishedSubtasks.isNotEmpty()) {
                    TextButton(
                        onClick = { showFinishedSubtasks = !showFinishedSubtasks },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Finished Tasks (${finishedSubtasks.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Icon(
                                imageVector = if (showFinishedSubtasks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    AnimatedVisibility(visible = showFinishedSubtasks) {
                        Column {
                            finishedSubtasks.forEach { subTask ->
                                SubTaskItem(subTask, task, actions)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubTaskItem(
    subTask: SubTask,
    parentTask: TaskItem,
    actions: TasksActions
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (subTask.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        
        Spacer(Modifier.width(12.dp))

        Text(
            text = subTask.title,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (subTask.completed) TextDecoration.LineThrough else TextDecoration.None,
            color = if (subTask.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Subtask Checkbox
        Surface(
            onClick = {
                scope.launch {
                    actions.toggleSubTask(parentTask, subTask.id)
                }
            },
            shape = RoundedCornerShape(4.dp),
            color = if (subTask.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
            border = BorderStroke(
                1.dp,
                if (subTask.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.size(24.dp)
        ) {
            if (subTask.completed) {
                Icon(Icons.Default.Check, "Completed", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

