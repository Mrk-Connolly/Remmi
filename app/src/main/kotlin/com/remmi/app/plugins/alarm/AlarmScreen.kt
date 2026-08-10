package com.remmi.app.plugins.alarm

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AlarmScreen(actions: AlarmActions) {
    val scope = rememberCoroutineScope()
    var alarms by remember { mutableStateOf(emptyList<AlarmItem>()) }
    var editorMode by remember { mutableStateOf<AlarmEditorMode?>(null) }

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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                        items(alarms, key = { it.id }) { alarm ->
                            AlarmRow(
                                alarm = alarm,
                                onClick = { editorMode = AlarmEditorMode.Edit(alarm) },
                                onDelete = {
                                    scope.launch {
                                        actions.deleteAlarm(alarm.id)
                                        alarms = actions.getAllAlarms()
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

@Composable
fun AlarmRow(
    alarm: AlarmItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = alarm.time.toLocalDateTime(timeZone)
    val timeStr = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
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
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
