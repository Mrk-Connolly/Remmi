package com.remmi.app.plugins.alarm.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.core.eventBus.commands.DeleteAlarmCommand
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.alarm.AlarmUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(
    actions: AlarmActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[AlarmScreen] - [AlarmScreen] executed")
    val scope = rememberCoroutineScope()
    var alarms by remember { mutableStateOf(emptyList<AlarmUiModel>()) }
    var editorMode by remember { mutableStateOf<AlarmEditorMode?>(null) }
    
    var isRefreshing by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                alarms = actions.getAllAlarms()
                delay(500) // Small delay for visual feedback
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        alarms = actions.getAllAlarms()
    }

    if (editorMode != null) {
        AlarmScreenEditor(
            mode = editorMode!!,
            actions = actions,
            controller = controller,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    alarms = actions.getAllAlarms()
                    editorMode = null
                }
            }
        )
    } else {
        RemmiHomeScreen(
            title = "Alarms",
            floatingActionButton = {
                RemmiFAB(
                    onClick = { editorMode = AlarmEditorMode.Create },
                    icon = Icons.Default.Add,
                    modifier = Modifier.padding(bottom = 16.dp),
                    contentDescription = "Add Alarm"
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (alarms.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No alarms set.", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(alarms, key = { it.alarm.id }) { uiModel ->
                                AlarmRow(
                                    uiModel = uiModel,
                                    onClick = { 
                                        if (!uiModel.isLocal) {
                                            editorMode = AlarmEditorMode.Edit(uiModel.alarm)
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            controller.eventBus.publishCommand(
                                                DeleteAlarmCommand(alarmId = uiModel.alarm.id)
                                            )
                                            alarms = actions.getAllAlarms()
                                        }
                                    },
                                    onLongClick = {
                                        if (uiModel.isLocal) {
                                            scope.launch {
                                                actions.openSystemAlarmApp()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmRow(
    uiModel: AlarmUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    Log.d("Remmi", "[AlarmScreen] - [AlarmRow] executed")
    val alarm = uiModel.alarm
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = alarm.time.toLocalDateTime(timeZone)
    val timeStr = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"

    val cardColor = if (alarm.isPriority) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                    else if (uiModel.isLocal) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface

    RemmiCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        containerColor = cardColor
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = if (alarm.isPriority) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (alarm.isPriority) {
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Priority",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (uiModel.isLocal) {
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                "SYSTEM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (alarm.description.isNotEmpty()) {
                    Text(
                        text = alarm.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            if (!uiModel.isLocal) {
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
