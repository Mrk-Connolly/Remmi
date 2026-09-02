package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.ui.components.RemmiFAB
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.models.CalendarItem
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.format.TextStyle
import java.util.Locale

enum class CalendarViewMode {
    MONTH
}

/**
 * Main screen for the Calendar plugin.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    actions: CalendarActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[CalendarScreen] - [CalendarScreen] executed")
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    var groups by remember { mutableStateOf(emptyList<CalendarGroup>()) }
    var editorMode by remember { mutableStateOf<CalendarEditorMode?>(null) }
    
    var viewMode by rememberSaveable { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showWeeklyGrid by rememberSaveable { mutableStateOf(false) }

    val now = remember { Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val calendarState = rememberSelectableCalendarState()
    val listState = rememberLazyListState()
    
    var isRefreshing by remember { mutableStateOf(false) }

    val refreshData: suspend () -> Unit = {
        events = actions.getAllEvents()
        groups = actions.getCalendarGroups()
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

    // Ensure groups are updated if changed elsewhere
    LaunchedEffect(groups) {
        Log.d("Remmi", "[CalendarScreen] - Groups updated: ${groups.size}")
    }

    if (editorMode != null) {
        CalendarScreenEditor(
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
    } else if (showWeeklyGrid) {
        WeeklyScheduleScreen(
            actions = actions,
            controller = controller,
            onBack = { showWeeklyGrid = false },
            onAddEvent = { date: LocalDate, startTime: LocalTime?, endTime: LocalTime? ->
                editorMode = CalendarEditorMode.CreateOnDate(date, startTime, endTime)
            },
            onEditEvent = { event: CalendarItem ->
                editorMode = CalendarEditorMode.Edit(event)
            }
        )
    } else {
        RemmiHomeScreen(
            title = "",
            floatingActionButton = {
                RemmiFAB(
                    onClick = { editorMode = CalendarEditorMode.Create },
                    icon = Icons.Default.Add,
                    modifier = Modifier.padding(bottom = 16.dp),
                    contentDescription = "Add Event"
                )
            }
        ) { padding ->
            val today = now
            
            val filteredEvents = remember(events, selectedFilter) {
                if (selectedFilter == "All") events else events.filter { it.group == selectedFilter }
            }
            
            val groupedEventsList = remember(filteredEvents, today) {
                val grouped = filteredEvents.groupBy { it.startingDate }.toMutableMap()
                if (!grouped.containsKey(today)) {
                    grouped[today] = emptyList()
                }
                grouped.toSortedMap().toList()
            }

            // Highlighting Logic based on scroll (Direct mapping from the very first visible item)
            val activeDate by remember {
                derivedStateOf {
                    val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()
                    if (firstVisible != null) {
                        // Find which group this index belongs to
                        var count = 0
                        var matchedDate = today
                        for (entry in groupedEventsList) {
                            if (firstVisible.index >= count) {
                                matchedDate = entry.first
                            }
                            count += 1 // Header
                            count += entry.second.size // Items
                            if (count > firstVisible.index) break
                        }
                        matchedDate
                    } else today
                }
            }

            val todayLayoutIndex by remember(groupedEventsList, today) {
                derivedStateOf {
                    var count = 0
                    var found = -1
                    for (entry in groupedEventsList) {
                        if (entry.first == today) {
                            found = count
                            break
                        }
                        count += 1
                        count += entry.second.size
                    }
                    found
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Unified Header Section
                CalendarHeader(
                    monthState = calendarState.monthState,
                    viewMode = viewMode,
                    onViewModeToggle = {
                        showWeeklyGrid = true
                    },
                    existingGroups = groups.map { it.name },
                    onFilterSelected = { selectedFilter = it }
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        // Calendar Section (Compact with active shading)
                        if (viewMode == CalendarViewMode.MONTH) {
                            Box(modifier = Modifier
                                .padding(horizontal = 40.dp, vertical = 4.dp)
                                .animateContentSize()
                            ) {
                                SelectableCalendar(
                                    calendarState = calendarState,
                                    monthHeader = { /* Hide internal header */ },
                                    dayContent = { dayState ->
                                        CalendarDay(
                                            dayState = dayState,
                                            eventsOnDay = events.filter { it.startingDate == dayState.date.toKotlinLocalDate() },
                                            isActiveViewDate = dayState.date.toKotlinLocalDate() == activeDate,
                                            onDayClick = { date ->
                                                // Scroll to this date or the next closest in the list
                                                val targetEntry = groupedEventsList.find { it.first >= date }
                                                if (targetEntry != null) {
                                                    val targetDate = targetEntry.first
                                                    val targetIndex = groupedEventsList.indexOfFirst { it.first == targetDate }
                                                    if (targetIndex != -1) {
                                                        var layoutIndex = 0
                                                        for (i in 0 until targetIndex) {
                                                            layoutIndex += 1 // Header
                                                            layoutIndex += groupedEventsList[i].second.size // Items
                                                        }
                                                        scope.launch {
                                                            listState.animateScrollToItem(layoutIndex)
                                                        }
                                                    }
                                                }
                                            },
                                            onLongDayClick = { date ->
                                                editorMode = CalendarEditorMode.CreateOnDate(date)
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        // Upcoming Events Area (Weighted to fill space under monthly view)
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (filteredEvents.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No events scheduled.")
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    groupedEventsList.forEachIndexed { _, (date, eventsOnDate) ->
                                        val isActive = date == activeDate
                                        
                                        stickyHeader(key = date.toString()) { 
                                            DateHeader(date, isToday = date == today, isActive = isActive) 
                                        }
                                        
                                        item(key = "${date}_box") {
                                            // Big square highlight around events of the same day
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                                    .padding(bottom = 12.dp)
                                                    .then(
                                                        if (isActive) Modifier
                                                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(DesignTokens.CornerRadiusMedium))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.02f), RoundedCornerShape(DesignTokens.CornerRadiusMedium))
                                                            .padding(vertical = 8.dp)
                                                        else Modifier
                                                    )
                                            ) {
                                                Column {
                                                    eventsOnDate.forEach { event ->
                                                        val groupColorHex = groups.find { it.name == event.group }?.colorHex
                                                        EventRow(
                                                            event = event,
                                                            groupColor = if (groupColorHex != null) Color(android.graphics.Color.parseColor(groupColorHex)) else MaterialTheme.colorScheme.primary,
                                                            isHighlighted = isActive,
                                                            onClick = { editorMode = CalendarEditorMode.Edit(event) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Floating Back to Today button (Now relative to upcoming section)
                            val isTodayVisible by remember(todayLayoutIndex) {
                                derivedStateOf {
                                    todayLayoutIndex != -1 && listState.layoutInfo.visibleItemsInfo.any { it.index == todayLayoutIndex }
                                }
                            }
                            
                            val isPastToday by remember(todayLayoutIndex) {
                                derivedStateOf {
                                    todayLayoutIndex != -1 && listState.firstVisibleItemIndex > todayLayoutIndex
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = !isTodayVisible && todayLayoutIndex != -1,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            ) {
                                SmallFloatingActionButton(
                                    onClick = {
                                        scope.launch {
                                            calendarState.monthState.currentMonth = java.time.YearMonth.now()
                                            calendarState.selectionState.onDateSelected(java.time.LocalDate.now())
                                            if (todayLayoutIndex != -1) {
                                                listState.animateScrollToItem(todayLayoutIndex)
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPastToday) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = "Back to Today",
                                        modifier = Modifier.size(20.dp)
                                    )
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
fun CalendarHeader(
    monthState: MonthState,
    viewMode: CalendarViewMode,
    onViewModeToggle: () -> Unit,
    existingGroups: List<String>,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onViewModeToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (viewMode == CalendarViewMode.MONTH) Icons.Default.ViewWeek else Icons.Default.CalendarMonth,
                contentDescription = "Toggle View",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { monthState.currentMonth = monthState.currentMonth.minusMonths(1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", modifier = Modifier.size(24.dp))
            }
            
            val monthName = remember(monthState.currentMonth) {
                monthState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            }
            Text(
                text = "$monthName ${monthState.currentMonth.year}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(onClick = { monthState.currentMonth = monthState.currentMonth.plusMonths(1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", modifier = Modifier.size(24.dp))
            }
        }

        var showFilterMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { showFilterMenu = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                DropdownMenuItem(text = { Text("All") }, onClick = { onFilterSelected("All"); showFilterMenu = false })
                existingGroups.forEach { group ->
                    DropdownMenuItem(text = { Text(group) }, onClick = { onFilterSelected(group); showFilterMenu = false })
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    dayState: io.github.boguszpawlowski.composecalendar.day.DayState<DynamicSelectionState>,
    eventsOnDay: List<CalendarItem>,
    isActiveViewDate: Boolean,
    onDayClick: (LocalDate) -> Unit,
    onLongDayClick: (LocalDate) -> Unit
) {
    val date = dayState.date.toKotlinLocalDate()
    val isSelected = dayState.selectionState.isDateSelected(dayState.date)
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection / Current day Circle highlight (Static and circled as requested)
        Box(
            modifier = Modifier
                .size(32.dp)
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
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    onClick = { 
                        dayState.selectionState.onDateSelected(dayState.date)
                        onDayClick(date)
                    },
                    onLongClick = {
                        onLongDayClick(date)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (eventsOnDay.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: LocalDate, isToday: Boolean, isActive: Boolean) {
    val dayName = remember(date) {
        val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
        javaDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    
    val monthName = remember(date) {
        java.time.Month.of(date.monthNumber).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    val headerText = if (isToday) "Today, ${date.dayOfMonth} $monthName" else "$dayName, ${date.dayOfMonth} $monthName"

    // Smooth selection animations
    val targetColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val textColor by animateColorAsState(targetValue = targetColor, label = "textColor")
    val dividerAlpha by animateFloatAsState(targetValue = if (isActive) 0.2f else 0f, label = "dividerAlpha")
    val textScale by animateFloatAsState(targetValue = if (isActive) 1.05f else 1f, label = "textScale")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background // Solid background for sticky headers
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .scale(textScale),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isActive) {
                // Premium centered design
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).height(1.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = dividerAlpha)
                    ) {}
                    
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Surface(
                        modifier = Modifier.weight(1f).height(1.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = dividerAlpha)
                    ) {}
                }
            } else {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun EventRow(event: CalendarItem, groupColor: Color, isHighlighted: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isHighlighted) 1f else 0.98f, label = "scale")

    RemmiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale),
        containerColor = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Priority Icon (Right of title)
                        if (event.isPriority) {
                            Surface(
                                modifier = Modifier.padding(horizontal = 8.dp).size(20.dp),
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "!",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (event.group != null) {
                            Surface(
                                modifier = Modifier.padding(start = 4.dp),
                                color = groupColor.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = event.group,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = groupColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (event.startingTime != null) {
                        val start = event.startingTime.toString().substring(0, 5)
                        val end = event.endingTime?.toString()?.substring(0, 5)
                        val timeStr = if (end != null) "$start - $end" else start

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

fun java.time.LocalDate.toKotlinLocalDate(): LocalDate = 
    LocalDate(year, monthValue, dayOfMonth)

sealed class CalendarEditorMode {
    data object Create : CalendarEditorMode()
    data class CreateOnDate(
        val date: LocalDate, 
        val startTime: LocalTime? = null, 
        val endTime: LocalTime? = null
    ) : CalendarEditorMode()
    data class Edit(val event: CalendarItem) : CalendarEditorMode()
}
