package com.remmi.app.plugins.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(actions: CalendarActions) {

    var editorMode by remember { mutableStateOf<EditorMode?>(null) }
    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
    }

    val groupedEvents = remember(events) {
        events.filter { it.startingTime != null }
            .groupBy { it.startingTime!!.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .toSortedMap()
    }

    val unscheduledEvents = remember(events) {
        events.filter { it.startingTime == null }
    }

    val dateToIndexMap = remember(groupedEvents, unscheduledEvents) {
        val map = mutableMapOf<LocalDate, Int>()
        var currentIndex = 0
        if (unscheduledEvents.isNotEmpty()) currentIndex += 1 + unscheduledEvents.size
        groupedEvents.forEach { (date, eventsOnDate) ->
            map[date] = currentIndex
            currentIndex += 1 + eventsOnDate.size
        }
        map
    }

    if (editorMode != null) {
        CalendarEditorScreen(
            mode = editorMode!!,
            actions = actions,
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
                    onClick = {
                        editorMode = EditorMode.Create(null)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SelectableCalendar(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    dayContent = { dayState ->
                        CalendarDay(
                            dayState = dayState,
                            eventsOnDay = groupedEvents[dayState.date.toKotlinLocalDate()] ?: emptyList(),
                            onDayClick = { date ->
                                scope.launch {
                                    dateToIndexMap[date]?.let { index ->
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            },
                            onDayLongClick = { date ->
                                editorMode = EditorMode.Create(date)
                            }
                        )
                    }
                )

                HorizontalDivider()

                Text(
                    text = "Upcoming",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f), state = listState) {
                    if (events.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No upcoming events", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (unscheduledEvents.isNotEmpty()) {
                        item {
                            Text("Unscheduled Events", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        items(unscheduledEvents, key = { it.id }) { item ->
                            EventCard(item = item, onClick = { selectedEvent = item })
                        }
                    }
                    groupedEvents.forEach { (date, eventsOnDate) ->
                        item {
                            Text("Day ${date.day}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        items(eventsOnDate, key = { it.id }) { item ->
                            EventCard(item = item, onClick = { selectedEvent = item })
                        }
                    }
                }
            }
        }
    }

    if (selectedEvent != null) {
        EventDetailDialog(
            event = selectedEvent!!,
            onDismiss = { selectedEvent = null },
            onDelete = {
                scope.launch {
                    actions.removeEvent(selectedEvent!!.id)
                    events = actions.getAllEvents()
                    selectedEvent = null
                }
            },
            onEdit = {
                editorMode = EditorMode.Edit(selectedEvent!!)
                selectedEvent = null
            }
        )
    }
}

sealed class EditorMode {
    data class Create(val initialDate: LocalDate? = null) : EditorMode()
    data class Edit(val event: CalendarItem) : EditorMode()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarDay(
    dayState: DayState<out DynamicSelectionState>,
    eventsOnDay: List<CalendarItem>,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: (LocalDate) -> Unit
) {
    val date = dayState.date.toKotlinLocalDate()
    val selectionState = dayState.selectionState
    val isSelected = selectionState.isDateSelected(dayState.date)

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (dayState.isCurrentDay) 1.dp else 0.dp,
                color = if (dayState.isCurrentDay) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .combinedClickable(
                onClick = {
                    selectionState.onDateSelected(dayState.date)
                    onDayClick(date)
                },
                onLongClick = {
                    onDayLongClick(date)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )

        if (eventsOnDay.isNotEmpty()) {
            val highestPriority = eventsOnDay.maxBy { it.priority }.priority
            val dotColor = getPriorityColor(highestPriority)

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}

@Composable
fun EventCard(
    item: CalendarItem,
    onClick: () -> Unit
) {
    val priorityColor = getPriorityColor(item.priority)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = priorityColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, priorityColor)
    ) {
        Text(
            text = item.title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> Color(0xFFE57373)   // Red
        Priority.NORMAL -> Color(0xFFFFD54F) // Yellow
        Priority.LOW -> Color(0xFF81C784)    // Green
    }
}

@Composable
fun EventDetailDialog(
    event: CalendarItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val startDateTime = event.startingTime?.toLocalDateTime(timeZone)
    val endDateTime = event.endingTime?.toLocalDateTime(timeZone)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                if (event.description.isNotEmpty()) {
                    Text(text = event.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                }
                Text(text = "Date: ${startDateTime?.date}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Time: ${startDateTime?.time} - ${endDateTime?.time ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Priority: ${event.priority.name}", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            Button(onClick = onEdit) { Text("Edit") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}
