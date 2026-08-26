package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Main screen for the Calendar plugin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    actions: CalendarActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarScreen] executed")
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    var editorMode by remember { mutableStateOf<CalendarEditorMode?>(null) }
    
    // Track editor state for hiding bottom menu
    LaunchedEffect(editorMode) {
        com.remmi.app.core.controller.GlobalUIState.isEditorActive.value = editorMode != null
    }

    DisposableEffect(Unit) {
        onDispose {
            com.remmi.app.core.controller.GlobalUIState.isEditorActive.value = false
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                events = actions.getAllEvents()
                delay(500)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
    }

    if (editorMode != null) {
        CalendarScreenEditor(
            mode = editorMode!!,
            actions = actions,
            controller = controller,
            onDismiss = { editorMode = null },
            onSave = {
                scope.launch {
                    events = actions.getAllEvents()
                    editorMode = null
                }
            }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { editorMode = CalendarEditorMode.Create },
                    modifier = Modifier.padding(bottom = 176.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        ) { padding ->
            val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            
            val groupedEvents = remember(events) {
                events.groupBy { it.startingDate }.toSortedMap()
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    if (events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No events scheduled.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 180.dp)
                        ) {
                            // Ensure Today is always visible if it has events or just as a header
                            if (!groupedEvents.containsKey(today)) {
                                item { DateHeader(today, isToday = true) }
                            }

                            groupedEvents.forEach { (date, eventsOnDate) ->
                                item { DateHeader(date, isToday = date == today) }
                                items(eventsOnDate) { event ->
                                    EventRow(event, onClick = { editorMode = CalendarEditorMode.Edit(event) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate, isToday: Boolean) {
    Text(
        text = if (isToday) "Today - ${date.dayOfMonth} ${date.month}" else "${date.dayOfWeek.name}, ${date.dayOfMonth} ${date.month}",
        style = if (isToday) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) 
                else MaterialTheme.typography.labelLarge,
        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun EventRow(event: CalendarItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isPriority) MaterialTheme.colorScheme.errorContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (event.isPriority) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (event.isPriority) {
                    Icon(
                        Icons.Default.PriorityHigh,
                        contentDescription = "Priority",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (event.description.isNotEmpty()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            val timeStr = event.startingTime?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: "All Day"
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

sealed class CalendarEditorMode {
    data object Create : CalendarEditorMode()
    data class Edit(val event: CalendarItem) : CalendarEditorMode()
}
