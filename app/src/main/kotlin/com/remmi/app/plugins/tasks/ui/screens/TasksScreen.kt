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
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.DesignTokens
import com.remmi.app.core.eventBus.commands.DeleteTaskCommand
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.ui.components.RemmiSectionHeader
import com.remmi.app.plugins.tasks.TasksActions
import com.remmi.app.plugins.tasks.models.TaskItem
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
    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var editorMode by remember { mutableStateOf<TaskEditorMode?>(null) }
    
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

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.background
        )
    )

    if (editorMode != null) {
        if (editorMode == TaskEditorMode.Multitask) {
            MultitaskEditorScreen(
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
        }
    } else {
        RemmiHomeScreen(
            title = "",
            backgroundBrush = backgroundBrush,
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RemmiFAB(
                        onClick = { editorMode = TaskEditorMode.Multitask },
                        icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp),
                        contentDescription = "Add Multitask"
                    )
                    RemmiFAB(
                        onClick = { editorMode = TaskEditorMode.Create },
                        icon = Icons.Default.Add,
                        modifier = Modifier.padding(bottom = 16.dp),
                        contentDescription = "Add Task"
                    )
                }
            }
        ) { padding ->
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            val taskSections = remember(filteredTasks) {
                val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val endOfWeek = today.plus(7, DateTimeUnit.DAY)
                val currentMonth = today.month
                val currentYear = today.year

                // Only show top-level tasks in sections
                val topLevelTasks = filteredTasks.filter { it.parentTask == null }

                val ongoing = topLevelTasks.filter { it.dueDate == null && !it.completed }.sortedByDescending { it.created }
                val daily = topLevelTasks.filter { 
                    it.dueDate != null && !it.completed &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date == today 
                }.sortedBy { it.dueDate }
                val thisWeek = topLevelTasks.filter { 
                    it.dueDate != null && !it.completed &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date > today &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date <= endOfWeek
                }.sortedBy { it.dueDate }
                val thisMonth = topLevelTasks.filter {
                    it.dueDate != null && !it.completed &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date > endOfWeek &&
                    it.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.let { d -> d.month == currentMonth && d.year == currentYear }
                }.sortedBy { it.dueDate }
                val completed = topLevelTasks.filter { it.completed }.sortedByDescending { it.completedAt ?: it.modified }
                
                listOf(
                    "Ongoing" to ongoing,
                    "Today" to daily,
                    "This Week" to thisWeek,
                    "This Month" to thisMonth,
                    "Finished" to completed
                ).filter { it.second.isNotEmpty() }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { onlyImportant = !onlyImportant }
                        ) {
                            Icon(
                                imageVector = if (onlyImportant) Icons.Default.PriorityHigh else Icons.Default.PriorityHigh,
                                contentDescription = "Filter Important",
                                tint = if (onlyImportant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                                    onClick = {
                                        selectedGroupFilter = "All"
                                        isFilterExpanded = false
                                    }
                                )
                                existingGroups.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g) },
                                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
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
                            Text("No tasks found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            taskSections.forEach { (sectionName, tasksInSection) ->
                                item {
                                    RemmiSectionHeader(
                                        title = sectionName,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                }
                                items(tasksInSection, key = { it.id }) { task ->
                                    val subtasks = tasks.filter { it.parentTask == task.id }
                                    if (subtasks.isNotEmpty()) {
                                        TaskGroupRow(
                                            parentTask = task,
                                            subtasks = subtasks,
                                            actions = actions,
                                            onUpdate = { tasks = it },
                                            onLongClick = { taskToManage = task }
                                        )
                                    } else {
                                        TaskRow(task, actions, onUpdate = { tasks = it }, onLongClick = { taskToManage = task })
                                    }
                                    Spacer(Modifier.height(DesignTokens.SpacingMedium))
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskGroupRow(
    parentTask: TaskItem,
    subtasks: List<TaskItem>,
    actions: TasksActions,
    onUpdate: (List<TaskItem>) -> Unit,
    onLongClick: () -> Unit
) {
    Log.d("Remmi", "[TasksScreen] - [TaskGroupRow] executed")
    var showFinished by remember { mutableStateOf(false) }

    val ongoingSubtasks = subtasks.filter { !it.completed }
    val finishedSubtasks = subtasks.filter { it.completed }

    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Could toggle expansion of the group if we wanted */ },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Project/Group Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = parentTask.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            // Ongoing Sub-tasks
            ongoingSubtasks.forEach { subtask ->
                SubtaskRow(subtask, actions, onUpdate)
                Spacer(Modifier.height(8.dp))
            }

            // Finished Sub-tasks Section
            if (finishedSubtasks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showFinished = !showFinished },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Finished Tasks (${finishedSubtasks.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Icon(
                            imageVector = if (showFinished) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                AnimatedVisibility(visible = showFinished) {
                    Column {
                        finishedSubtasks.forEach { subtask ->
                            SubtaskRow(subtask, actions, onUpdate)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubtaskRow(
    task: TaskItem,
    actions: TasksActions,
    onUpdate: (List<TaskItem>) -> Unit
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot or Circle on the left
        Box(
            modifier = Modifier
                .size(8.dp)
                .padding(2.dp)
                .background(
                    color = if (task.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        
        Spacer(Modifier.width(12.dp))

        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
            color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Small Square Checkbox
        Surface(
            onClick = {
                scope.launch {
                    actions.toggleTask(task)
                    onUpdate(actions.getAllTasks())
                }
            },
            shape = RoundedCornerShape(4.dp),
            color = if (task.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
            border = BorderStroke(
                1.dp,
                if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.size(24.dp)
        ) {
            if (task.completed) {
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
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (task.isPriority) FontWeight.Bold else FontWeight.Medium
                )

                // Show Group and Subgroup
                if (task.group != null || task.subgroup != null) {
                    val groupText = listOfNotNull(task.group, task.subgroup).joinToString(" • ")
                    Text(
                        text = groupText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (task.dueDate != null) {
                    val dueDateStr =
                        task.dueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    Text(
                        text = if (isOverdue) "Overdue: $dueDateStr" else "Due: $dueDateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        ),
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded && task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Square Checkbox on the Right
            Surface(
                onClick = {
                    isCompleted = !isCompleted
                    scope.launch {
                        actions.toggleTask(task)
                        onUpdate(actions.getAllTasks())
                    }
                },
                shape = RoundedCornerShape(8.dp),
                color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = BorderStroke(
                    2.dp,
                    if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    )
                ),
                modifier = Modifier.size(32.dp)
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}
