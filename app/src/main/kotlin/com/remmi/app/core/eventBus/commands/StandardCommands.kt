package com.remmi.app.core.eventBus.commands

import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.plugins.calendar.models.CalendarItem
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import java.util.UUID

// ----------------------------------------------------------------------------
//                               SYSTEM COMMANDS
// ----------------------------------------------------------------------------

/**
 * SAVE DATA COMMAND
 * Request to perform a global data save.
 */
data class SaveDataCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * UPSERT DATA COMMAND
 * Request to insert or update a RemmiModel in a specific table.
 */
data class UpsertDataCommand<T : RemmiModel>(
    val tableName: String,
    val item: T,
    val serializer: KSerializer<T>,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * DELETE DATA COMMAND
 * Request to delete an item by ID from a specific table.
 */
data class DeleteDataCommand(
    val tableName: String,
    val itemId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * SYNC PLUGIN DATA COMMAND
 * Request a specific plugin to synchronize or load its data.
 */
data class SyncPluginDataCommand(
    val pluginId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH DATA BY ID COMMAND
 * Request to fetch a specific model by ID from a table.
 */
data class FetchDataByIdCommand<T : RemmiModel>(
    val tableName: String,
    val itemId: String,
    val serializer: KSerializer<T>,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH DATA BY SOURCE COMMAND
 * Request to fetch items linked to a specific source.
 */
data class FetchDataBySourceCommand<T : RemmiModel>(
    val tableName: String,
    val sourcePlugin: String,
    val sourceItemId: String,
    val serializer: KSerializer<T>,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH ALL DATA COMMAND
 * Request to fetch all items from a specific table.
 */
data class FetchAllDataCommand<T : RemmiModel>(
    val tableName: String,
    val serializer: KSerializer<T>,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               ANDROID SYSTEM COMMANDS
// ----------------------------------------------------------------------------

data class SetSystemAlarmCommand(
    val id: String,
    val title: String,
    val timeMillis: Long,
    val useSound: Boolean = true,
    val useVibration: Boolean = true,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class CancelSystemAlarmCommand(
    val id: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class SyncSystemClockCommand(
    val title: String,
    val timeMillis: Long,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class RemoveSystemClockCommand(
    val title: String,
    val timeMillis: Long,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class OpenSystemAlarmAppCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               AUTOMATION COMMANDS
// ----------------------------------------------------------------------------

/**
 * RUN DAILY BRIEFING COMMAND
 * Trigger the automation engine to generate and notify the daily briefing.
 */
data class RunDailyBriefingCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * RUN DATABASE CLEANUP COMMAND
 * Trigger the automation engine to clean up expired data across all plugins.
 */
data class RunDatabaseCleanupCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH TODAY'S TASKS COMMAND
 * Request from AutomationEngine to get tasks for the briefing.
 */
data class FetchTodayTasksCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH TODAY'S EVENTS COMMAND
 * Request from AutomationEngine to get calendar events for the briefing.
 */
data class FetchTodayEventsCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH WEEKLY EVENTS COMMAND
 */
data class FetchWeeklyEventsCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "lock_screen",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH WEEKLY TASKS COMMAND
 */
data class FetchWeeklyTasksCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "lock_screen",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * UPDATE LOCK SCREEN SUMMARY COMMAND
 */
data class UpdateLockScreenSummaryCommand(
    val summary: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "lock_screen",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * FETCH WEATHER COMMAND
 * Request weather data for the daily briefing.
 */
data class FetchWeatherCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * POST NOTIFICATION COMMAND
 * Request a system notification to be displayed.
 */
data class PostNotificationCommand(
    val title: String,
    val content: String,
    val useSound: Boolean = true,
    val useVibration: Boolean = true,
    val tag: String? = null,
    val ongoing: Boolean = false,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * POST LIVE UPDATE COMMAND
 * Request a progress-centric notification (Android 16+).
 */
data class PostLiveUpdateCommand(
    val title: String,
    val content: String,
    val progress: Int,
    val maxProgress: Int,
    val tag: String? = null,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               ALARM COMMANDS
// ----------------------------------------------------------------------------

data class CreateAlarmCommand(
    val title: String,
    val description: String,
    val time: Instant,
    val isPriority: Boolean = false,
    val repeatable: List<String> = emptyList(),
    val custom: List<String> = emptyList(),
    val syncToSystem: Boolean = true,
    val useSound: Boolean = true,
    val useVibration: Boolean = true,
    val sourcePlugin: String? = null,
    val sourceItemId: String? = null,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class UpdateAlarmCommand(
    val alarm: AlarmItem,
    val syncToSystem: Boolean = true,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class DeleteAlarmCommand(
    val alarmId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               CALENDAR COMMANDS
// ----------------------------------------------------------------------------

data class CreateCalendarEventCommand(
    val title: String,
    val description: String,
    val startingDate: LocalDate,
    val startingTime: LocalTime? = null,
    val endingDate: LocalDate? = null,
    val endingTime: LocalTime? = null,
    val isPriority: Boolean = false,
    val group: String? = null,
    val isRepeatable: Boolean = false,
    val repeatableType: String? = null,
    val participants: List<String> = emptyList(),
    val repeat: List<String> = emptyList(),
    val location: List<String> = emptyList(),
    val createLinkedTask: Boolean = false,
    val createLinkedAlarm: Boolean = false,
    val createLinkedLocation: Boolean = false,
    val createLinkedContact: Boolean = false,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class UpdateCalendarEventCommand(
    val event: CalendarItem,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class DeleteCalendarEventCommand(
    val eventId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               TASK COMMANDS
// ----------------------------------------------------------------------------

data class CreateTaskCommand(
    val title: String,
    val description: String,
    val dueDate: Instant? = null,
    val isPriority: Boolean = false,
    val group: String? = null,
    val repeat: RepeatRule? = null,
    val sourcePlugin: String? = null,
    val sourceItemId: String? = null,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class UpdateTaskCommand(
    val task: TaskItem,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class DeleteTaskCommand(
    val taskId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

data class ToggleTaskCommand(
    val taskId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               MAP COMMANDS
// ----------------------------------------------------------------------------

/**
 * PICK LOCATION COMMAND
 * Request the Map plugin to show the location picker.
 */
data class PickLocationCommand(
    val initialSearch: String? = null,
    val requestId: String = UUID.randomUUID().toString(),
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * REQUEST CURRENT LOCATION COMMAND
 * Request system location service to provide current coordinates.
 */
data class RequestLocationCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * SHOW MAP COMMAND
 * Navigate to the main map screen.
 */
data class ShowMapCommand(
    val focusId: String? = null,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               OCR / RECEIPT COMMANDS
// ----------------------------------------------------------------------------

/**
 * REQUEST RECEIPT IMAGE COMMAND
 * Request a receipt image from camera or gallery.
 */
data class RequestReceiptImageCommand(
    val useCamera: Boolean,
    val requestId: String = UUID.randomUUID().toString(),
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "ingredients",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

/**
 * REQUEST OCR COMMAND
 * Request OCR processing for a given image.
 */
data class RequestOCRCommand(
    val imageUri: String,
    val requestId: String = UUID.randomUUID().toString(),
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "ingredients",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand

// ----------------------------------------------------------------------------
//                               INGREDIENT COMMANDS
// ----------------------------------------------------------------------------

data class FetchIngredientMetadataCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system",
    override val correlationId: String? = null,
    override val causationId: String? = null,
    override val creationContext: CreationContext? = null,
    override val deletionContext: DeletionContext? = null
) : RemmiCommand
