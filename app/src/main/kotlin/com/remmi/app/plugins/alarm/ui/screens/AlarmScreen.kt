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
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.alarm.AlarmActions
import com.remmi.app.plugins.alarm.AlarmUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(
    actions: AlarmActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[AlarmScreen] - System Integrated")
    val scope = rememberCoroutineScope()
    var alarms by remember { mutableStateOf(emptyList<AlarmUiModel>()) }
    var nextSystemAlarm by remember { mutableStateOf<Long?>(null) }
    var editorMode by remember { mutableStateOf<AlarmEditorMode?>(null) }
    
    var isRefreshing by remember { mutableStateOf(false) }

    val refreshData: suspend () -> Unit = {
        alarms = actions.getAllAlarms()
        nextSystemAlarm = actions.getNextSystemAlarm()
    }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                refreshData()
                delay(500)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.background
        )
    )

    if (editorMode != null) {
        AlarmScreenEditor(
            mode = editorMode!!,
            actions = actions,
            controller = controller,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    refreshData()
                    editorMode = null
                }
            }
        )
    } else {
        RemmiHomeScreen(
            title = "Alarms",
            backgroundBrush = backgroundBrush,
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
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Next System Alarm Card
                    item {
                        NextAlarmCard(nextSystemAlarm) {
                            scope.launch { actions.openSystemAlarmApp() }
                        }
                    }

                    if (alarms.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    "No Remmi alarms set.", 
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(alarms, key = { it.alarm.id }) { uiModel ->
                            AlarmRow(
                                uiModel = uiModel,
                                onClick = { 
                                    editorMode = AlarmEditorMode.Edit(uiModel.alarm)
                                },
                                onDelete = {
                                    scope.launch {
                                        actions.deleteAlarm(uiModel.alarm.id)
                                        refreshData()
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
fun NextAlarmCard(triggerTime: Long?, onClick: () -> Unit) {
    RemmiCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.NotificationImportant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Next System Alarm",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val timeStr = if (triggerTime != null) {
                    val date = Date(triggerTime)
                    val format = remember { SimpleDateFormat("EEE, HH:mm", Locale.getDefault()) }
                    format.format(date)
                } else "None scheduled"
                
                Text(
                    timeStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmRow(
    uiModel: AlarmUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val alarm = uiModel.alarm
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = alarm.time.toLocalDateTime(timeZone)
    val timeStr = "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"

    RemmiCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (alarm.isPriority) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
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
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val repeatText = when {
                    alarm.repeatable.contains("d") -> "Daily"
                    alarm.repeatable.contains("w") -> "Weekly"
                    alarm.repeatable.contains("c") -> "Custom: ${alarm.custom.joinToString(", ")}"
                    else -> "Once"
                }
                
                Text(
                    text = repeatText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
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
