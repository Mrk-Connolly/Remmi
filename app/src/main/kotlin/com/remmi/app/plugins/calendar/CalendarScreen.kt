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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remmi.app.core.model.components.Priority
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch

/**
 * Main Composable representing the full-screen UI for the Calendar plugin.
 *
 * It features a monthly monthly view at the top and a chronological timeline of
 * upcoming events below it. It also manages interactions for adding, viewing,
 * and deleting events.
 *
 * @param actions The action controller providing data and operations for this screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(actions: CalendarActions) {

    var showAddDialog by remember { mutableStateOf(false) }
    var prefilledDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    val listState = rememberLazyListState()

    // Group events by date for the timeline view.
    val groupedEvents = remember(events) {
        events.filter { it.startingTime != null }
            .groupBy { it.startingTime!!.toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .toSortedMap()
    }

    // Filter events that don't have a specific starting time.
    val unscheduledEvents = remember(events) {
        events.filter { it.startingTime == null }
    }

    // Map dates to their first occurrence in the LazyColumn for the "Scroll to Date" feature.
    val dateToIndexMap = remember(groupedEvents, unscheduledEvents) {
        val map = mutableMapOf<LocalDate, Int>()
        var currentIndex = 0
        
        if (unscheduledEvents.isNotEmpty()) {
            currentIndex += 1 + unscheduledEvents.size
        }

        groupedEvents.forEach { (date, eventsOnDate) ->
            map[date] = currentIndex
            currentIndex += 1 + eventsOnDate.size
        }
        map
    }

    // Refresh events when the screen is first loaded.
    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Event"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Monthly Calendar View
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
                            prefilledDate = date
                            showAddDialog = true
                        }
                    )
                }
            )

            HorizontalDivider()

            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Chronological timeline of events.
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState
            ) {

                // Empty state.
                if (events.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No upcoming events",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section for unscheduled events.
                if (unscheduledEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Unscheduled ----",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(unscheduledEvents, key = { it.id }) { item ->
                        EventCard(
                            item = item,
                            onClick = { selectedEvent = item }
                        )
                    }
                }

                // Daily event sections.
                groupedEvents.forEach { (date, eventsOnDate) ->

                    item {
                        Text(
                            text = "Date ${date.day} ----",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(eventsOnDate, key = { it.id }) { item ->
                        EventCard(
                            item = item,
                            onClick = { selectedEvent = item }
                        )
                    }
                }

            }

        }

    }

    // Dialog for viewing event details.
    if (selectedEvent != null) {
        EventDetailDialog(
            event = selectedEvent!!,
            onDismiss = {
                selectedEvent = null
            },
            onDelete = {
                scope.launch {
                    actions.removeEvent(selectedEvent!!.id)
                    events = actions.getAllEvents()
                    selectedEvent = null
                }
            }
        )
    }

    // Dialog for creating a new event.
    if (showAddDialog) {
        AddEventDialog(
            initialDate = prefilledDate,
            onDismiss = {
                showAddDialog = false
                prefilledDate = null
            },

            onSave = { title, description, day, month, year, startTime, endTime, priority ->
                scope.launch {
                    actions.addEvent(title, description, day, month, year, startTime, endTime, priority)
                    events = actions.getAllEvents()
                    showAddDialog = false
                    prefilledDate = null
                }
            }
        )
    }
}

/**
 * Custom day cell Composable for the monthly calendar.
 *
 * Highlights the selected day, marks the current day, and displays priority-coded
 * dots for days with scheduled events.
 */
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

        // Show a priority-coded dot if the day has events.
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

/**
 * Small UI card representing a single calendar event in the timeline.
 * The card's color and border reflect the event's priority.
 */
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

/**
 * Maps a [Priority] level to a specific [Color].
 */
fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> Color(0xFFE57373)   // Red
        Priority.NORMAL -> Color(0xFFFFD54F) // Yellow
        Priority.LOW -> Color(0xFF81C784)    // Green
    }
}

/**
 * Dialog Composable for entering information to create a new calendar event.
 *
 * @param initialDate Optional date to pre-fill the form (e.g., from a long press on the calendar).
 */
@Composable
fun AddEventDialog( initialDate: LocalDate? = null,
                    onDismiss: () -> Unit,
                    onSave: (
                        title: String,
                        description: String,
                        day: String,
                        month: String,
                        year: String,
                        startTime: String,
                        endTime: String,
                        priority: Priority
                    ) -> Unit

) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(initialDate?.day?.toString() ?: "") }
    var month by remember { mutableStateOf(initialDate?.month?.let { it.ordinal + 1 }?.toString() ?: "") }
    var year by remember { mutableStateOf(initialDate?.year?.toString() ?: "") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.NORMAL) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Calendar Event")
        },
        text = {
            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(Modifier.height(16.dp))
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Month") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = startTime,
                        onValueChange = { startTime = it
                        },
                        label = { Text("Start (HH:mm)")
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = endTime,
                        onValueChange = { endTime = it
                        },
                        label = { Text("End (HH:mm)")
                        }
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Priority")
                Spacer(Modifier.height(4.dp))
                Row {
                    Priority.entries.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == it,
                                onClick = { priority = it
                                }
                            )
                            Text(it.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        description,
                        day,
                        month,
                        year,
                        startTime,
                        endTime,
                        priority
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog Composable for displaying the full details of a selected [CalendarItem].
 * Provides an option to delete the event.
 */
@Composable
fun EventDetailDialog(
    event: CalendarItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val startDateTime = event.startingTime?.toLocalDateTime(timeZone)
    val endDateTime = event.endingTime?.toLocalDateTime(timeZone)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(event.title)
        },
        text = {
            Column {
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    text = "Date: ${startDateTime?.date}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Time: ${startDateTime?.time} - ${endDateTime?.time ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Priority: ${event.priority.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    )
}
