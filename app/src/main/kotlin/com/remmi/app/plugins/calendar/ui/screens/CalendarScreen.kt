package com.remmi.app.plugins.calendar.ui.screens

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
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.DeleteCalendarEventCommand
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.CalendarItem
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate as JavaLocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    actions: CalendarActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarScreen] executed")
    var editorMode by remember { mutableStateOf<EditorMode?>(null) }
    
    // Track editor state for hiding bottom menu
    LaunchedEffect(editorMode) {
        controller.isEditorActive.value = editorMode != null
    }

    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    val today = remember { JavaLocalDate.now().toKotlinLocalDate() }

    var selectedGroupFilter by remember { mutableStateOf("All") }
    var existingGroups by remember { mutableStateOf(emptyList<String>()) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                events = actions.getAllEvents()
                existingGroups = actions.getAllGroups()
                delay(500)
                isRefreshing = false
            }
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
    
    // Auto-scroll to today on first load
    LaunchedEffect(events, dateToIndexMap) {
        if (events.isNotEmpty()) {
            dateToIndexMap[today]?.let { index ->
                listState.scrollToItem(index)
            }
        }
    }

    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val todayIndex = remember(dateToIndexMap) { dateToIndexMap[today] ?: -1 }
    
    val showJumpToTodayTop by remember {
        derivedStateOf { 
            todayIndex != -1 && firstVisibleIndex > todayIndex + 5 
        }
    }
    val showJumpToTodayBottom by remember {
        derivedStateOf { 
            todayIndex != -1 && firstVisibleIndex < todayIndex - 5 
        }
    }

    if (editorMode != null) {
        CalendarEditorScreen(
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
                    onClick = {
                        editorMode = EditorMode.Create(null)
                    },
                    modifier = Modifier.padding(bottom = 156.dp) // Offset above island menu
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
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
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Integrated Filter and Calendar Header
                        SelectableCalendar(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            monthHeader = { monthState ->
                                CalendarHeader(
                                    monthState = monthState,
                                    selectedGroupFilter = selectedGroupFilter,
                                    existingGroups = existingGroups,
                                    onFilterSelected = { selectedGroupFilter = it }
                                )
                            },
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

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                                    val isToday = date == today
                                    Text(
                                        text = if (isToday) "Today" else "Day ${date.day}", 
                                        style = MaterialTheme.typography.labelLarge, 
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, 
                                        textAlign = TextAlign.Center, 
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                                items(eventsOnDate, key = { it.id }) { item ->
                                    EventCard(
                                        item = item, 
                                        onClick = { /* Keep standard tap for accessibility? */ },
                                        onLongClick = { selectedEvent = item }
                                    )
                                }
                            }
                        }
                    }

                    // Jump to Today Floating Helpers
                    if (showJumpToTodayTop) {
                        Button(
                            onClick = { scope.launch { listState.animateScrollToItem(todayIndex) } },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Today, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Back to Today")
                        }
                    }

                    if (showJumpToTodayBottom) {
                        Button(
                            onClick = { scope.launch { listState.animateScrollToItem(todayIndex) } },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 220.dp), // Above FAB and Menu
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Today, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Back to Today")
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
                    controller.eventBus.publishCommand(
                        DeleteCalendarEventCommand(eventId = selectedEvent!!.id)
                    )
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

@Composable
fun CalendarHeader(
    monthState: MonthState,
    selectedGroupFilter: String,
    existingGroups: List<String>,
    onFilterSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { monthState.currentMonth = monthState.currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, null)
            }
            
            Text(
                text = monthState.currentMonth.month
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                    .lowercase()
                    .replaceFirstChar { it.titlecase() } + " " + monthState.currentMonth.year,
                style = MaterialTheme.typography.titleLarge,
            )

            IconButton(onClick = { monthState.currentMonth = monthState.currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }

        var isFilterExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = { isFilterExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = isFilterExpanded,
                onDismissRequest = { isFilterExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All") },
                    onClick = {
                        onFilterSelected("All")
                        isFilterExpanded = false
                    }
                )
                existingGroups.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g) },
                        onClick = {
                            onFilterSelected(g)
                            isFilterExpanded = false
                        }
                    )
                }
            }
        }
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
            .aspectRatio(1.2f) // Slightly shorter cells
            .padding(1.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCard(
    item: CalendarItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Log.d("Remmi", "[CalendarScreen] - [EventCard] executed")
    val cardColor = if (item.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (item.isPriority) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
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
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
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
