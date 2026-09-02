package com.remmi.app.plugins.calendar.ui.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiSecondaryScreen
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.calendar.models.CalendarGroup
import com.remmi.app.plugins.calendar.models.CalendarItem
import kotlinx.datetime.*
import java.time.format.TextStyle
import java.util.Locale

private val HourHeight = 64.dp

/**
 * WEEKLY SCHEDULE SCREEN
 * A grid-based view showing events across the week (Mon-Sun).
 */
@Composable
fun WeeklyScheduleScreen(
    actions: CalendarActions,
    controller: RemmiController,
    onBack: () -> Unit,
    onAddEvent: (LocalDate, LocalTime?, LocalTime?) -> Unit,
    onEditEvent: (CalendarItem) -> Unit
) {
    Log.d("Remmi", "[WeeklyScheduleScreen] - Executed")
    var events by remember { mutableStateOf(emptyList<CalendarItem>()) }
    var groups by remember { mutableStateOf(emptyList<CalendarGroup>()) }
    
    val today = remember { 
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        now.toLocalDateTime(TimeZone.currentSystemDefault()).date 
    }
    
    val startOfWeek = remember(today) { getStartOfWeek(today) }
    val weekDays = remember(startOfWeek) { (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) } }

    val scrollState = rememberScrollState()

    // Initial scroll to midday (12:00)
    LaunchedEffect(Unit) {
        val density = controller.androidContext.resources.displayMetrics.density
        val targetOffset = (HourHeight.value * 12 * density).toInt()
        scrollState.scrollTo(targetOffset)
    }

    LaunchedEffect(Unit) {
        events = actions.getAllEvents()
        groups = actions.getCalendarGroups()
    }

    RemmiSecondaryScreen(
        title = "Weekly Schedule",
        onBack = onBack,
        topBarActions = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Month View", tint = MaterialTheme.colorScheme.primary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Day Headers (Sticky)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.width(50.dp)) // Time column spacer
                weekDays.forEach { date ->
                    DayHeaderItem(
                        date = date,
                        isToday = date == today,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. All-day Events Area (Sticky)
            val allDayEvents = remember(events, startOfWeek) {
                events.filter { it.startingTime == null && it.startingDate in startOfWeek..startOfWeek.plus(6, DateTimeUnit.DAY) }
            }
            
            if (allDayEvents.isNotEmpty()) {
                AllDayEventsArea(
                    events = allDayEvents,
                    weekDays = weekDays,
                    groups = groups,
                    onEventClick = onEditEvent
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 3. Hour Grid (Scrollable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Background Grid Lines
                Column {
                    (0..23).forEach { hour ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HourHeight)
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            Text(
                                text = "${hour.toString().padStart(2, '0')}:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp)
                                    .width(40.dp)
                            )
                        }
                    }
                }

                // Grid interaction Layer
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 50.dp) // Offset for time column
                ) {
                    weekDays.forEach { date ->
                        DayGridColumn(
                            date = date,
                            events = events.filter { it.startingDate == date && it.startingTime != null },
                            groups = groups,
                            onSlotClick = { hour ->
                                onAddEvent(date, LocalTime(hour, 0), LocalTime((hour + 1) % 24, 0))
                            },
                            onRangeSelected = { startHour, endHour ->
                                onAddEvent(date, LocalTime(startHour, 0), LocalTime(endHour % 24, 0))
                            },
                            onEventClick = onEditEvent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeaderItem(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val dayName = remember(date) { 
        val javaDay = java.time.DayOfWeek.of(date.dayOfWeek.isoDayNumber)
        javaDay.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AllDayEventsArea(
    events: List<CalendarItem>,
    weekDays: List<LocalDate>,
    groups: List<CalendarGroup>,
    onEventClick: (CalendarItem) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Spacer(modifier = Modifier.width(50.dp))
            weekDays.forEach { date ->
                val dayEvents = events.filter { it.startingDate == date }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    dayEvents.forEach { event ->
                        val groupColorHex = groups.find { it.name == event.group }?.colorHex
                        val color = if (groupColorHex != null) Color(android.graphics.Color.parseColor(groupColorHex)) else MaterialTheme.colorScheme.primary
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(color.copy(alpha = 0.8f))
                                .clickable { onEventClick(event) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayGridColumn(
    date: LocalDate,
    events: List<CalendarItem>,
    groups: List<CalendarGroup>,
    onSlotClick: (Int) -> Unit,
    onRangeSelected: (Int, Int) -> Unit,
    onEventClick: (CalendarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectionStartHour by remember { mutableStateOf<Int?>(null) }
    var selectionEndHour by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        selectionStartHour = (offset.y / HourHeight.toPx()).toInt().coerceIn(0, 23)
                        selectionEndHour = selectionStartHour
                    },
                    onDrag = { change, _ ->
                        selectionEndHour = (change.position.y / HourHeight.toPx()).toInt().coerceIn(0, 23)
                    },
                    onDragEnd = {
                        val start = selectionStartHour
                        val end = selectionEndHour
                        if (start != null && end != null) {
                            onRangeSelected(minOf(start, end), maxOf(start, end) + 1)
                        }
                        selectionStartHour = null
                        selectionEndHour = null
                    },
                    onDragCancel = {
                        selectionStartHour = null
                        selectionEndHour = null
                    }
                )
            }
    ) {
        // Hour Slots for clicking
        Column {
            (0..23).forEach { hour ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HourHeight)
                        .clickable { onSlotClick(hour) }
                )
            }
        }

        // Selection Highlight
        if (selectionStartHour != null && selectionEndHour != null) {
            val start = minOf(selectionStartHour!!, selectionEndHour!!)
            val end = maxOf(selectionStartHour!!, selectionEndHour!!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = HourHeight * start)
                    .height(HourHeight * (end - start + 1))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            )
        }

        // Events
        events.forEach { event ->
            val start = event.startingTime ?: return@forEach
            val end = event.endingTime ?: LocalTime((start.hour + 1) % 24, start.minute)
            
            val startMinutes = start.hour * 60 + start.minute
            val endMinutes = end.hour * 60 + end.minute
            val duration = if (endMinutes > startMinutes) endMinutes - startMinutes else 60
            
            val yOffset = (startMinutes.toFloat() / 60f) * HourHeight.value
            val height = (duration.toFloat() / 60f) * HourHeight.value
            
            val groupColorHex = groups.find { it.name == event.group }?.colorHex
            val color = if (groupColorHex != null) Color(android.graphics.Color.parseColor(groupColorHex)) else MaterialTheme.colorScheme.primary

            Surface(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .offset(y = yOffset.dp)
                    .height(height.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.9f),
                onClick = { onEventClick(event) }
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (duration >= 45) {
                        Text(
                            text = "${start.hour.toString().padStart(2, '0')}:${start.minute.toString().padStart(2, '0')} - ${end.hour.toString().padStart(2, '0')}:${end.minute.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private fun getStartOfWeek(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.isoDayNumber // 1 (Mon) to 7 (Sun)
    return date.minus(dayOfWeek - 1, DateTimeUnit.DAY)
}
