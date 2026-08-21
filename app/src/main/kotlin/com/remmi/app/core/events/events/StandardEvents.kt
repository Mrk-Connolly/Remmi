package com.remmi.app.core.events.events

import com.remmi.app.core.events.EventType
import com.remmi.app.core.service.android.WeatherInfo
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.tasks.TaskItem
import java.util.UUID

// ----------------------------------------------------------------------------
//                               ALARM EVENTS
// ----------------------------------------------------------------------------

data class AlarmCreatedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class AlarmUpdatedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class AlarmDeletedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               CALENDAR EVENTS
// ----------------------------------------------------------------------------

data class CalendarEventCreatedEvent(
    val itemId: String,
    val isPriority: Boolean,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class CalendarEventUpdatedEvent(
    val itemId: String,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class CalendarEventDeletedEvent(
    val itemId: String,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               TASK EVENTS
// ----------------------------------------------------------------------------

data class TaskCreatedEvent(
    val taskId: String,
    val priority: Boolean,
    val group: String? = null,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class TaskUpdatedEvent(
    val taskId: String,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class TaskDeletedEvent(
    val taskId: String,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               AUTOMATION EVENTS
// ----------------------------------------------------------------------------

data class TodayTasksFetchedEvent(
    val tasks: List<TaskItem>,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED
) : RemmiEvent

data class TodayEventsFetchedEvent(
    val events: List<CalendarItem>,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED
) : RemmiEvent

data class WeatherFetchedEvent(
    val weather: WeatherInfo,
    override val source: String = "android",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED
) : RemmiEvent

data class DailyBriefingGeneratedEvent(
    val summary: String,
    override val source: String = "automation",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent
