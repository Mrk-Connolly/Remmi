package com.remmi.app.core.events.events

import com.remmi.app.core.events.EventType
import com.remmi.app.core.service.android.WeatherInfo
import com.remmi.app.core.model.calendar.CalendarItem
import com.remmi.app.core.model.tasks.TaskItem
import com.remmi.app.core.model.ingredients.IngredientMetadata
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
//                               GIFT EVENTS
// ----------------------------------------------------------------------------

data class GiftIdeaCreatedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class GiftIdeaUpdatedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class GiftIdeaDeletedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               RECIPE EVENTS
// ----------------------------------------------------------------------------

data class RecipeCreatedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class RecipeUpdatedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class RecipeDeletedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               INGREDIENT EVENTS
// ----------------------------------------------------------------------------

data class IngredientCreatedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED
) : RemmiEvent

data class IngredientUpdatedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class IngredientStockAdjustedEvent(
    val itemId: String,
    val delta: Double,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
) : RemmiEvent

data class IngredientDeletedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED
) : RemmiEvent

data class IngredientMetadataFetchedEvent(
    val metadata: List<IngredientMetadata>,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               MAP EVENTS
// ----------------------------------------------------------------------------

data class LocationPickedEvent(
    val requestId: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    override val source: String = "maps",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED
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

data class WeeklyEventsFetchedEvent(
    val events: List<CalendarItem>,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED
) : RemmiEvent

data class WeeklyTasksFetchedEvent(
    val tasks: List<TaskItem>,
    override val source: String = "tasks",
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
