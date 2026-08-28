package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.models.CalendarItem
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.format.TextStyle
import java.util.Locale

enum class CalendarViewMode {
    MONTH, WEEK
}

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
    var groups by remember { mutableStateOf(emptyList<CalendarGroup>()) }
    var editorMode by remember { mutableStateOf<CalendarEditorMode?>(null) }
    
    var viewMode by rememberSaveable { mutableStateOf(CalendarViewMode.MONTH) }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    
    val now = remember { Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val calendarState = rememberSelectableCalendarState()
    val listState = rememberLazyListState()
    
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
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { editorMode = CalendarEditorMode.Create },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
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

            // Highlighting Logic based on scroll (Center-based as requested)
            val activeDate by remember {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    if (visibleItems.isNotEmpty()) {
                        val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                        
                        // Find item closest to center
                        val centerItem = visibleItems.minByOrNull { item ->
                            val itemCenter = item.offset + item.size / 2
                            kotlin.math.abs(itemCenter - viewportCenter)
                        }
                        
                        if (centerItem != null) {
                            // Find which group this index belongs to
                            var count = 0
                            var matchedDate = today
                            for (entry in groupedEventsList) {
                                if (centerItem.index >= count) {
                                    matchedDate = entry.first
                                }
                                count += 1 // Header
                                count += entry.second.size // Items
                                if (count > centerItem.index) break
                            }
                            matchedDate
                        } else today
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
                    .padding(bottom = padding.calculateBottomPadding())
                    .statusBarsPadding()
            ) {
                // Unified Header Section
                CalendarHeader(
                    monthState = calendarState.monthState,
                    viewMode = viewMode,
                    onViewModeToggle = {
                        viewMode = if (viewMode == CalendarViewMode.MONTH) CalendarViewMode.WEEK else CalendarViewMode.MONTH
                    },
                    existingGroups = groups.map { it.name },
                    onFilterSelected = { selectedFilter = it }
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                                                onDayClick = { /* Selected in state */ }
                                            )
                                        }
                                    )
                                }
                            }

                            // Events List
                            if (filteredEvents.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No events scheduled.")
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    groupedEventsList.forEachIndexed { _, (date, eventsOnDate) ->
                                        val isActive = date == activeDate
                                        
                                        item(key = date.toString()) { 
                                            DateHeader(date, isToday = date == today, isActive = isActive) 
                                        }
                                        
                                        item(key = "${date}_box") {
                                            // Big square highlight around events of the same day
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp)
                                                    .then(
                                                        if (isActive) Modifier
                                                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
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
                        }

                        // Floating Back to Today button
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
                                elevation = FloatingActionButtonDefaults.elevation(4.dp)
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
    onDayClick: (LocalDate) -> Unit
) {
    val date = dayState.date.toKotlinLocalDate()
    val isSelected = dayState.selectionState.isDateSelected(dayState.date)
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Active View Highlight (Shading as requested, same size as selection circle)
        if (isActiveViewDate) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
            )
        }

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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    onClick = { 
                        dayState.selectionState.onDateSelected(dayState.date)
                        onDayClick(date)
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isActive) {
            // ---- Day ---- design
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }
        } else {
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelLarge,
                color = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EventRow(event: CalendarItem, groupColor: Color, isHighlighted: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isHighlighted) 1f else 0.95f, label = "scale")
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(if (isHighlighted) 2.dp else 1.dp, groupColor.copy(alpha = if (isHighlighted) 1f else 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (event.startingTime != null) {
                        val start = event.startingTime.toString().substring(0, 5)
                        val end = event.endingTime?.toString()?.substring(0, 5)
                        val timeStr = if (end != null) "$start - $end" else start
                        
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (event.group != null) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(groupColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = event.group,
                            style = MaterialTheme.typography.labelSmall,
                            color = groupColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (event.isPriority) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color.Red, CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
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
    data class Edit(val event: CalendarItem) : CalendarEditorMode()
}
