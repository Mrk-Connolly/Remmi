package com.remmi.app.plugins.alarm.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.alarm.AlarmUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(actions: AlarmActions) {
    Log.d("Remmi", "[AlarmScreen] - [AlarmScreen] executed")
    val scope = rememberCoroutineScope()
    var alarms by remember { mutableStateOf(emptyList<AlarmUiModel>()) }
    var editorMode by remember { mutableStateOf<AlarmEditorMode?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            alarms = actions.getAllAlarms()
            delay(500) // Small delay for visual feedback
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        alarms = actions.getAllAlarms()
    }

    if (editorMode != null) {
        AlarmScreenEditor(
            mode = editorMode!!,
            actions = actions,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    alarms = actions.getAllAlarms()
                    editorMode = null
                }
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { editorMode = AlarmEditorMode.Create }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                }
            },
            bottomBar = {
                Spacer(Modifier.height(96.dp))
            }
        ) { padding ->
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
                    Text(
                        text = "My Alarms",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    if (alarms.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No alarms yet. Tap + to add one.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                                            actions.deleteAlarm(uiModel.alarm.id)
                                            alarms = actions.getAllAlarms()
                                        }
                                    },
                                    onLongClick = {
                                        if (uiModel.isLocal) {
                                            actions.openSystemAlarmApp()
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

    val cardColor = if (alarm.isPriority) MaterialTheme.colorScheme.errorContainer else if (uiModel.isLocal) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (alarm.isPriority) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (alarm.isPriority) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Priority",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (uiModel.isLocal) {
                        Spacer(Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { onLongClick() },
                            label = { Text("Local Alarm", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (alarm.description.isNotEmpty()) {
                    Text(
                        text = alarm.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (!uiModel.isLocal) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
