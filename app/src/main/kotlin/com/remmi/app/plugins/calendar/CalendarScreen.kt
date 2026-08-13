package com.remmi.app.plugins.calendar

import android.util.Log
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(actions: CalendarActions) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarScreen] executed")
    var editorMode by remember { mutableStateOf<EditorMode?>(null) }
    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedGroupFilter by remember { mutableStateOf("All") }
    var existingGroups by remember { mutableStateOf(emptyList<String>()) }

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            events = actions.getAllEvents()
            existingGroups = actions.getAllGroups()
            delay(500)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
        existingGroups = actions.getAllGroups()
    }

    val filteredEvents = remember(events, selectedGroupFilter) {
        if (selectedGroupFilter == "All") events
        else events.filter { it.group == selectedGroupFilter }
    }

    val groupedEvents = remember(filteredEvents) {
        filteredEvents.groupBy { it.startingDate }
            .toSortedMap()
    }

    val dateToIndexMap = remember(groupedEvents) {
        val map = mutableMapOf<LocalDate, Int>()
        var currentIndex = 0
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarDay(
    dayState: DayState<out DynamicSelectionState>,
    eventsOnDay: List<CalendarItem>,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: (LocalDate) -> Unit
) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarDay] executed")
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
            val hasPriority = eventsOnDay.any { it.isPriority }
            val dotColor = if (hasPriority) Color.Red else MaterialTheme.colorScheme.primary

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
    Log.d("Remmi", "[CalendarScreen] - [EventCard] executed")
    val cardColor = if (item.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (item.isPriority) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isPriority) FontWeight.Bold else FontWeight.Normal
                )
                if (item.group != null) {
                    Text(
                        text = item.group,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (item.isPriority) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = "Priority",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EventDetailDialog(
    event: CalendarItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Log.d("Remmi", "[CalendarScreen] - [EventDetailDialog] executed")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title) },
        text = {
            Column {
                if (event.description.isNotEmpty()) {
                    Text(text = event.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                }
                Text(text = "Date: ${event.startingDate}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Time: ${event.startingTime ?: "N/A"} - ${event.endingTime ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Priority: ${if (event.isPriority) "High" else "Normal"}", style = MaterialTheme.typography.bodyMedium)
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
