package com.remmi.app.core.events

import com.remmi.app.core.plugins.model.components.RepeatRule
import com.remmi.app.plugins.alarm.AlarmItem
import com.remmi.app.plugins.calendar.CalendarItem
import com.remmi.app.plugins.tasks.TaskItem
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
    override val source: String = "system"
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
    override val source: String = "system"
) : RemmiCommand

/**
 * FETCH TODAY'S TASKS COMMAND
 * Request from AutomationEngine to get tasks for the briefing.
 */
data class FetchTodayTasksCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation"
) : RemmiCommand

/**
 * FETCH TODAY'S EVENTS COMMAND
 * Request from AutomationEngine to get calendar events for the briefing.
 */
data class FetchTodayEventsCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation"
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
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class UpdateAlarmCommand(
    val alarm: AlarmItem,
    val syncToSystem: Boolean = true,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class DeleteAlarmCommand(
    val alarmId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
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
    val participants: List<String> = emptyList(),
    val repeat: List<String> = emptyList(),
    val location: List<String> = emptyList(),
    val linkedTasks: List<String> = emptyList(),
    val linkedAlarm: String? = null,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class UpdateCalendarEventCommand(
    val event: CalendarItem,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class DeleteCalendarEventCommand(
    val eventId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
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
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class UpdateTaskCommand(
    val task: TaskItem,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand

data class DeleteTaskCommand(
    val taskId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand
