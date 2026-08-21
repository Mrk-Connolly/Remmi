package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.DeleteCalendarEventCommand
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.CalendarItem
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.datetime.*
import java.time.DayOfWeek
import java.time.LocalDate as JavaLocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures

enum class CalendarViewMode {
    MONTH, WEEK
}

fun isAllDay(event: CalendarItem): Boolean {
    val start = event.startingTime ?: LocalTime(0, 0)
    val end = event.endingTime ?: LocalTime(23, 59)
    return start.hour == 0 && start.minute == 0 && end.hour == 23 && end.minute == 59
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    actions: CalendarActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarScreen] executed")
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val today = remember { JavaLocalDate.now().toKotlinLocalDate() }
    
    var viewMode by rememberSaveable { mutableStateOf(CalendarViewMode.MONTH) }
    
    val currentWeekStart = rememberSaveable(
        saver = Saver<MutableState<LocalDate>, String>(
            save = { it.value.toString() },
            restore = { mutableStateOf(LocalDate.parse(it)) }
        )
    ) { 
        mutableStateOf(JavaLocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toKotlinLocalDate()) 
    }

    var selectedGroupFilter by rememberSaveable { mutableStateOf("All") }

    var editorMode by remember { mutableStateOf<EditorMode?>(null) }
    
    // Track editor state for hiding bottom menu
    LaunchedEffect(editorMode) {
        controller.isEditorActive.value = editorMode != null
    }

    LaunchedEffect(viewMode) {
        controller.isMenuVisible.value = viewMode != CalendarViewMode.WEEK
    }
    
    DisposableEffect(Unit) {
        onDispose {
            controller.isMenuVisible.value = true
            controller.isEditorActive.value = false
        }
    }

    var selectedEvent by remember { mutableStateOf<CalendarItem?>(null) }
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

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

    val groupedEvents = remember(filteredEvents, today) {
        val groups = filteredEvents.groupBy { it.startingDate }.toMutableMap()
        if (!groups.containsKey(today)) {
            groups[today] = emptyList()
        }
        groups.toSortedMap()
    }

    val calendarGridEvents = remember(filteredEvents) {
        val map = mutableMapOf<LocalDate, MutableList<CalendarItem>>()
        filteredEvents.forEach { event ->
            val start = event.startingDate
            val end = event.endingDate ?: start
            var current = start
            while (current <= end) {
                map.getOrPut(current) { mutableListOf() }.add(event)
                try {
                    val javaDate = java.time.LocalDate.of(current.year, current.monthNumber, current.dayOfMonth).plusDays(1)
                    current = LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
                } catch (e: Exception) {
                    break
                }
                if (map.size > 10000) break // safety
            }
        }
        map
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
    
    val activeDate by remember(dateToIndexMap, firstVisibleIndex) {
        derivedStateOf {
            dateToIndexMap.entries
                .filter { it.value <= firstVisibleIndex }
                .maxByOrNull { it.value }
                ?.key ?: today
        }
    }

    val todayIndex = remember(dateToIndexMap) { dateToIndexMap[today] ?: -1 }
    
    val showJumpToToday by remember {
        derivedStateOf { 
            todayIndex != -1 && (firstVisibleIndex > todayIndex + 1 || firstVisibleIndex < todayIndex - 1)
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
                if (viewMode == CalendarViewMode.MONTH) {
                    FloatingActionButton(
                        onClick = {
                            editorMode = EditorMode.Create(activeDate)
                        },
                        modifier = Modifier.padding(bottom = 168.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Event")
                    }
                }
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Integrated Filter and Calendar Header
                        if (viewMode == CalendarViewMode.MONTH || !isLandscape) {
                            if (viewMode == CalendarViewMode.MONTH) {
                                SelectableCalendar(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    monthHeader = { monthState ->
                                        CalendarHeader(
                                            monthState = monthState,
                                            viewMode = viewMode,
                                            currentWeekStart = currentWeekStart.value,
                                            onViewModeChange = { viewMode = it },
                                            onWeekNavigate = { currentWeekStart.value = it },
                                            selectedGroupFilter = selectedGroupFilter,
                                            existingGroups = existingGroups,
                                            onFilterSelected = { selectedGroupFilter = it }
                                        )
                                    },
                                    dayContent = { dayState ->
                                        CalendarDay(
                                        dayState = dayState,
                                        eventsOnDay = calendarGridEvents[dayState.date.toKotlinLocalDate()] ?: emptyList(),
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
                            } else {
                                CalendarHeader(
                                    monthState = null,
                                    viewMode = viewMode,
                                    currentWeekStart = currentWeekStart.value,
                                    onViewModeChange = { viewMode = it },
                                    onWeekNavigate = { currentWeekStart.value = it },
                                    selectedGroupFilter = selectedGroupFilter,
                                    existingGroups = existingGroups,
                                    onFilterSelected = { selectedGroupFilter = it }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        if (viewMode == CalendarViewMode.MONTH) {
                            AnimatedVisibility(
                                visible = showJumpToToday,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    TextButton(
                                        onClick = { scope.launch { listState.animateScrollToItem(todayIndex) } },
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Today, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Back to Today", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f), 
                                state = listState,
                                contentPadding = PaddingValues(bottom = 180.dp)
                            ) {
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
                                    val isActive = date == activeDate
                                    
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isToday) "Today" else "Day ${date.day}", 
                                            style = if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.labelLarge, 
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) MaterialTheme.colorScheme.primary else if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary, 
                                            textAlign = TextAlign.Center
                                        )
                                        if (isToday && eventsOnDate.isEmpty()) {
                                            Text(
                                                text = "No events",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
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
                        } else {
                            WeekScheduleView(
                                weekStart = currentWeekStart.value,
                                events = filteredEvents,
                                onEventClick = { selectedEvent = it },
                                onEventLongClick = { editorMode = EditorMode.Edit(it) },
                                onEmptySpaceLongClick = { startDate, startTime, endDate, endTime -> 
                                    editorMode = EditorMode.Create(startDate, startTime, endDate, endTime) 
                                },
                                modifier = Modifier.weight(1f)
                            )
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
    monthState: MonthState?,
    viewMode: CalendarViewMode,
    currentWeekStart: LocalDate,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onWeekNavigate: (LocalDate) -> Unit,
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
        // View Toggle Button (Left)
        IconButton(
            onClick = { 
                onViewModeChange(if (viewMode == CalendarViewMode.MONTH) CalendarViewMode.WEEK else CalendarViewMode.MONTH)
            },
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
        ) {
            Icon(
                imageVector = if (viewMode == CalendarViewMode.MONTH) Icons.Default.ViewWeek else Icons.Default.CalendarMonth,
                contentDescription = "Switch View",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { 
                if (viewMode == CalendarViewMode.MONTH) {
                    monthState?.currentMonth = monthState?.currentMonth?.minusMonths(1)!!
                } else {
                    onWeekNavigate(currentWeekStart.toJavaLocalDate().minusWeeks(1).toKotlinLocalDate())
                }
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, null)
            }
            
            val title = if (viewMode == CalendarViewMode.MONTH) {
                val currentMonth = monthState?.currentMonth
                if (currentMonth != null) {
                    java.time.Month.of(currentMonth.month.number)
                        .getDisplayName(TextStyle.FULL, Locale.getDefault())
                        .lowercase()
                        .replaceFirstChar { it.titlecase() } + " " + currentMonth.year
                } else null
            } else {
                val end = currentWeekStart.toJavaLocalDate().plusDays(6)
                "${currentWeekStart.day} ${java.time.Month.of(currentWeekStart.month.number).getDisplayName(TextStyle.SHORT, Locale.getDefault())} - ${end.dayOfMonth} ${end.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
            }

            Text(
                text = title ?: "",
                style = MaterialTheme.typography.titleLarge,
            )

            IconButton(onClick = { 
                if (viewMode == CalendarViewMode.MONTH) {
                    monthState?.currentMonth = monthState?.currentMonth?.plusMonths(1)!!
                } else {
                    onWeekNavigate(currentWeekStart.toJavaLocalDate().plusWeeks(1).toKotlinLocalDate())
                }
            }) {
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }

        var isFilterExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
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
    
    val singleDayEvents = remember(eventsOnDay) {
        eventsOnDay.filter { it.endingDate == null || it.endingDate == it.startingDate }
    }
    val multiDayEvents = remember(eventsOnDay) {
        eventsOnDay.filter { it.endingDate != null && it.endingDate!! > it.startingDate }
    }

    // Connection logic for range background
    val connectsLeft = multiDayEvents.any { it.startingDate < date }
    val connectsRight = multiDayEvents.any { (it.endingDate ?: it.startingDate) > date }
    val isPriorityRange = multiDayEvents.any { it.isPriority }

    Box(
        modifier = Modifier
            .aspectRatio(1.2f)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Multi-day Event Range Background (Capsule)
        if (multiDayEvents.isNotEmpty()) {
            val shape = when {
                !connectsLeft && connectsRight -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                connectsLeft && !connectsRight -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                connectsLeft && connectsRight -> androidx.compose.ui.graphics.RectangleShape
                else -> CircleShape // Single date spanning (should be singleDayEvents)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .background(
                        color = if (isPriorityRange) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = shape
                    )
            )
        }

        // Selection / Today Indicator and Date Content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
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

            if (singleDayEvents.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    singleDayEvents.take(4).forEach { event ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    color = if (event.isPriority) Color.Red else MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
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
                if (!isAllDay(event)) {
                    Text(text = "Time: ${event.startingTime ?: "N/A"} - ${event.endingTime ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "Time: All Day", style = MaterialTheme.typography.bodyMedium)
                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekScheduleView(
    weekStart: LocalDate,
    events: List<CalendarItem>,
    onEventClick: (CalendarItem) -> Unit,
    onEventLongClick: (CalendarItem) -> Unit,
    onEmptySpaceLongClick: (LocalDate, LocalTime, LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current
    
    val days = remember(weekStart) {
        (0..6).map { weekStart.toJavaLocalDate().plusDays(it.toLong()).toKotlinLocalDate() }
    }
    
    val hourHeight = if (isLandscape) 96.dp else 32.dp
    val totalHeight = hourHeight * 24
    val scrollState = rememberScrollState()
    
    // Auto-scroll to current time (e.g. 8 AM) on first load
    LaunchedEffect(Unit) {
        scrollState.scrollTo((8 * hourHeight.value * density.density).toInt())
    }

    val (allDayEvents, timedEvents) = remember(events) {
        events.partition { isAllDay(it) }
    }

    var selectionRange by remember { mutableStateOf<SelectionRange?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Day Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp) // Offset for time column
                .background(MaterialTheme.colorScheme.surface)
        ) {
            days.forEach { date ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.toJavaLocalDate().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.day.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (date == JavaLocalDate.now().toKotlinLocalDate()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // All-Day Events Sticky Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            days.forEach { date ->
                val allDayOnDay = allDayEvents.filter { it.startingDate <= date && (it.endingDate ?: it.startingDate) >= date }
                val (multiDay, singleDay) = allDayOnDay.partition { it.endingDate != null && it.endingDate!! > it.startingDate }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 0.dp, vertical = 2.dp)
                ) {
                    // Multi-day events first (continuous line effect)
                    multiDay.forEach { event ->
                        val connectsL = event.startingDate < date
                        val connectsR = (event.endingDate ?: event.startingDate) > date
                        val shape = when {
                            !connectsL && connectsR -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                            connectsL && !connectsR -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                            connectsL && connectsR -> androidx.compose.ui.graphics.RectangleShape
                            else -> RoundedCornerShape(4.dp)
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (event.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = shape,
                            onClick = { onEventClick(event) }
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                    // Single-day all-day events below
                    singleDay.forEach { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (event.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(2.dp),
                            onClick = { onEventClick(event) }
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                    if (allDayOnDay.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            val gridColor = MaterialTheme.colorScheme.outlineVariant
            
            // Grid Container to ensure explicit height for selection and lines
            Box(modifier = Modifier.height(totalHeight).fillMaxWidth()) {
                
                // Background Grid lines
                Box(modifier = Modifier.fillMaxSize().padding(start = 48.dp)) {
                    // Vertical Lines
                    Row(modifier = Modifier.fillMaxSize()) {
                        repeat(7) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(width = 0.2.dp, color = gridColor.copy(alpha = 0.3f))
                            )
                        }
                    }
                    // Horizontal Lines
                    Column(modifier = Modifier.fillMaxSize()) {
                        repeat(24) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourHeight)
                                    .drawBehind {
                                        drawLine(
                                            color = gridColor.copy(alpha = 0.3f),
                                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    // Time Column
                    Column(modifier = Modifier.width(48.dp).fillMaxHeight().padding(start = 8.dp)) {
                        repeat(24) { hour ->
                            Box(
                                modifier = Modifier.height(hourHeight),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Text(
                                    text = "%02d:00".format(hour),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Day Columns with Drag Selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(days, hourHeight, density.density) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        val colWidth = size.width / 7f
                                        val dayIdx = (offset.x / colWidth).toInt().coerceIn(0, 6)
                                        val hour = (offset.y / density.density / hourHeight.value).toInt().coerceIn(0, 23)
                                        selectionRange = SelectionRange(
                                            startDate = days[dayIdx],
                                            startTime = LocalTime(hour, 0),
                                            endDate = days[dayIdx],
                                            endTime = LocalTime((hour + 1).coerceAtMost(23), 0)
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val colWidth = size.width / 7f
                                        val currentOffset = change.position
                                        val dayIdx = (currentOffset.x / colWidth).toInt().coerceIn(0, 6)
                                        val hour = (currentOffset.y / density.density / hourHeight.value).toInt().coerceIn(0, 23)
                                        
                                        selectionRange?.let { start ->
                                            selectionRange = start.copy(
                                                endDate = days[dayIdx],
                                                endTime = LocalTime((hour + 1).coerceAtMost(23), 0)
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        selectionRange?.let { range ->
                                            val startDateTime = LocalDateTime(range.startDate, range.startTime)
                                            val endDateTime = LocalDateTime(range.endDate, range.endTime)
                                            
                                            if (startDateTime <= endDateTime) {
                                                onEmptySpaceLongClick(range.startDate, range.startTime, range.endDate, range.endTime)
                                            } else {
                                                onEmptySpaceLongClick(range.endDate, range.endTime, range.startDate, range.startTime)
                                            }
                                        }
                                        selectionRange = null
                                    },
                                    onDragCancel = { selectionRange = null }
                                )
                            }
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            days.forEach { date ->
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    val eventsOnDay = timedEvents.filter { it.startingDate == date }
                                    eventsOnDay.forEach { event ->
                                        EventBox(
                                            event = event,
                                            hourHeight = hourHeight,
                                            isLandscape = isLandscape,
                                            onClick = { onEventClick(event) },
                                            onLongClick = { onEventLongClick(event) }
                                        )
                                    }
                                }
                            }
                        }

                        // Selection Overlay
                        selectionRange?.let { range ->
                            SelectionOverlay(
                                range = range,
                                days = days,
                                hourHeight = hourHeight
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SelectionRange(
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime
)

@Composable
fun SelectionOverlay(
    range: SelectionRange,
    days: List<LocalDate>,
    hourHeight: androidx.compose.ui.unit.Dp
) {
    val startDateTime = LocalDateTime(range.startDate, range.startTime)
    val endDateTime = LocalDateTime(range.endDate, range.endTime)
    
    val actualStart = if (startDateTime <= endDateTime) startDateTime else endDateTime
    val actualEnd = if (startDateTime <= endDateTime) endDateTime else startDateTime

    Row(modifier = Modifier.fillMaxSize()) {
        days.forEach { date ->
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (date >= actualStart.date && date <= actualEnd.date) {
                    val top = if (date == actualStart.date) {
                        (actualStart.time.hour * hourHeight.value).dp
                    } else 0.dp
                    
                    val bottom = if (date == actualEnd.date) {
                        (actualEnd.time.hour * hourHeight.value).dp
                    } else (24 * hourHeight.value).dp
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                            .offset(y = top)
                            .height(bottom - top)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventBox(
    event: CalendarItem,
    hourHeight: androidx.compose.ui.unit.Dp,
    isLandscape: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val startTime = event.startingTime ?: LocalTime(0, 0)
    val endTime = event.endingTime ?: LocalTime(23, 59)
    val isAllDayEvent = isAllDay(event)
    
    val startY = (startTime.hour * hourHeight.value + (startTime.minute / 60f) * hourHeight.value).dp
    val durationMinutes = if (event.endingDate != null && event.endingDate!! > event.startingDate) {
        // Multi-day events simplified to end of day for this view
        (24 * 60) - (startTime.hour * 60 + startTime.minute)
    } else {
        val startTotal = startTime.hour * 60 + startTime.minute
        val endTotal = endTime.hour * 60 + endTime.minute
        (endTotal - startTotal).coerceAtLeast(30)
    }
    val height = (durationMinutes / 60f * hourHeight.value).dp

    val containerColor = if (event.isPriority) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (event.isPriority) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .offset(y = startY)
            .height(height)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(4.dp),
        border = if (event.isPriority) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = if (isLandscape) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!isAllDayEvent && durationMinutes >= 45) {
                Text(
                    text = "${startTime.hour}:${"%02d".format(startTime.minute)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            if (durationMinutes >= 60) {
                if (event.group != null) {
                    Text(
                        text = event.group,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 7.sp,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 7.sp,
                        color = contentColor.copy(alpha = 0.6f),
                        maxLines = if (isLandscape) 3 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
