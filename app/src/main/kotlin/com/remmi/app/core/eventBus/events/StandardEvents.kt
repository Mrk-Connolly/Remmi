package com.remmi.app.core.eventBus.events

import com.remmi.app.core.eventBus.EventType
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem
import com.remmi.app.plugins.ingredients.models.IngredientMetadata
import java.util.UUID

/**
 * Metadata for requested linked items during a creation event.
 */
data class LinkedCreationRequest(
    val createAlarm: Boolean = false,
    val createTask: Boolean = false,
    val createLocation: Boolean = false,
    val createContact: Boolean = false
)

/**
 * DATA FETCHED EVENT
 * Fact that data was successfully retrieved from the database.
 */
data class DataFetchedEvent<T : RemmiModel>(
    val items: List<T>,
    val requestId: String, // Matches causationId/commandId
    override val source: String = "database",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               ALARM EVENTS
// ----------------------------------------------------------------------------

data class AlarmCreatedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class AlarmUpdatedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class AlarmDeletedEvent(
    val alarmId: String,
    override val source: String = "alarm",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               CALENDAR EVENTS
// ----------------------------------------------------------------------------

data class CalendarEventCreatedEvent(
    val itemId: String,
    val isPriority: Boolean,
    val linkedRequests: LinkedCreationRequest = LinkedCreationRequest(),
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class CalendarEventUpdatedEvent(
    val itemId: String,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class CalendarEventDeletedEvent(
    val itemId: String,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               CONTACT EVENTS
// ----------------------------------------------------------------------------

data class ContactCreatedEvent(
    val itemId: String,
    override val source: String = "contacts",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class ContactUpdatedEvent(
    val itemId: String,
    override val source: String = "contacts",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class ContactDeletedEvent(
    val itemId: String,
    override val source: String = "contacts",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
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
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class TaskUpdatedEvent(
    val taskId: String,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class TaskDeletedEvent(
    val taskId: String,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               GIFT EVENTS
// ----------------------------------------------------------------------------

data class GiftIdeaCreatedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class GiftIdeaUpdatedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class GiftIdeaDeletedEvent(
    val itemId: String,
    override val source: String = "gift",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               RECIPE EVENTS
// ----------------------------------------------------------------------------

data class RecipeCreatedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class RecipeUpdatedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class RecipeDeletedEvent(
    val itemId: String,
    override val source: String = "recipe_book",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               INGREDIENT EVENTS
// ----------------------------------------------------------------------------

data class IngredientCreatedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class IngredientUpdatedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class IngredientStockAdjustedEvent(
    val itemId: String,
    val delta: Double,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class IngredientDeletedEvent(
    val itemId: String,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.DELETED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class IngredientMetadataFetchedEvent(
    val metadata: List<IngredientMetadata>,
    override val source: String = "ingredient_stock",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
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
    override val type: EventType = EventType.UPDATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

/**
 * CURRENT LOCATION RESPONDED EVENT
 */
data class CurrentLocationRespondedEvent(
    val latitude: Double,
    val longitude: Double,
    override val source: String = "android",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               OCR / RECEIPT EVENTS
// ----------------------------------------------------------------------------

/**
 * RECEIPT IMAGE SELECTED EVENT
 */
data class ReceiptImageSelectedEvent(
    val imageUri: String,
    val requestId: String,
    override val source: String = "android",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

/**
 * RECEIPT TEXT RECOGNIZED EVENT
 */
data class ReceiptTextRecognizedEvent(
    val text: String,
    val requestId: String,
    override val source: String = "android",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

// ----------------------------------------------------------------------------
//                               AUTOMATION EVENTS
// ----------------------------------------------------------------------------

data class TodayTasksFetchedEvent(
    val tasks: List<TaskItem>,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class TodayEventsFetchedEvent(
    val events: List<CalendarItem>,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class WeeklyEventsFetchedEvent(
    val events: List<CalendarItem>,
    override val source: String = "calendar",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class WeeklyTasksFetchedEvent(
    val tasks: List<TaskItem>,
    override val source: String = "tasks",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class WeatherFetchedEvent(
    val weather: WeatherInfo,
    override val source: String = "android",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.LOADED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent

data class DailyBriefingGeneratedEvent(
    val summary: String,
    override val source: String = "automation",
    override val eventId: String = UUID.randomUUID().toString(),
    override val type: EventType = EventType.CREATED,
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiEvent
