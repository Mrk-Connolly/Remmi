package com.remmi.app.testing.plugins

import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import com.remmi.app.testing.base.BaseIntegrationTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.seconds

/**
 * RELATIONSHIP INTEGRATION TEST
 * 
 * Verifies the automated relationships between plugins (e.g., Calendar event creating an Alarm).
 */
class RelationshipIntegrationTest : BaseIntegrationTest() {

    @Test
    fun calendarEventCreated_withCreateAlarmFlag_createsAndCleansUpLinkedAlarm() = runTest {
        // Arrange
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Act 1: Create Calendar Event with createAlarm = true
        val eventId = calendarPlugin.actions.addEvent(
            title = "Meeting with Alarms",
            description = "Test Description",
            startingDate = today,
            startingTime = LocalTime(14, 0),
            createAlarm = true
        )
        assertThat(eventId).isNotNull()

        delay(2.seconds)

        // Assert 1: Verify Alarm was created
        val alarms = alarmPlugin.actions.getAllAlarms()
        val linkedAlarm = alarms.find { it.alarm.sourcePlugin == "calendar" && it.alarm.sourceItemId == eventId }
        assertThat(linkedAlarm).isNotNull()
        assertThat(linkedAlarm!!.alarm.title).isEqualTo("Alarm: Meeting with Alarms")

        // Act 2: Delete Calendar Event
        calendarPlugin.actions.removeEvent(eventId!!)
        delay(2.seconds)

        // Assert 2: Verify Alarm was deleted
        val alarmsAfterDelete = alarmPlugin.actions.getAllAlarms()
        val linkedAlarmAfterDelete = alarmsAfterDelete.find { it.alarm.sourcePlugin == "calendar" && it.alarm.sourceItemId == eventId }
        assertThat(linkedAlarmAfterDelete).isNull()
    }

    @Test
    fun calendarEventCreated_withCreateTaskFlag_createsAndCleansUpLinkedTask() = runTest {
        // Arrange
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val tasksPlugin = controller.pluginManager.plugins["tasks"] as TasksPlugin
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Act 1: Create Calendar Event with createTask = true
        val eventId = calendarPlugin.actions.addEvent(
            title = "Meeting with Task",
            description = "Test Task Description",
            startingDate = today,
            createTask = true
        )
        assertThat(eventId).isNotNull()

        delay(2.seconds)

        // Assert 1: Verify Task was created
        val tasks = tasksPlugin.actions.getAllTasks()
        val linkedTask = tasks.find { it.sourcePlugin == "calendar" && it.sourceItemId == eventId }
        assertThat(linkedTask).isNotNull()
        assertThat(linkedTask!!.title).isEqualTo("Task for: Meeting with Task")

        // Act 2: Delete Calendar Event
        calendarPlugin.actions.removeEvent(eventId!!)
        delay(2.seconds)

        // Assert 2: Verify Task was deleted
        val tasksAfterDelete = tasksPlugin.actions.getAllTasks()
        val linkedTaskAfterDelete = tasksAfterDelete.find { it.sourcePlugin == "calendar" && it.sourceItemId == eventId }
        assertThat(linkedTaskAfterDelete).isNull()
    }

    @Test
    fun linkedAlarmDeleted_manuallyByUsers_doesNotDeleteSourceCalendarEvent() = runTest {
        // Arrange
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val eventId = calendarPlugin.actions.addEvent(
            title = "Source Event",
            description = "Standalone Desc",
            startingDate = today,
            startingTime = LocalTime(10, 0),
            createAlarm = true
        )
        
        delay(5.seconds)
        val linkedAlarm = alarmPlugin.actions.getAllAlarms().find { it.alarm.sourceItemId == eventId }
        assertThat(linkedAlarm).isNotNull()

        // Act: Delete secondary manually
        alarmPlugin.actions.deleteAlarm(linkedAlarm!!.alarm.id)
        delay(1.seconds)

        // Assert: Verify source still exists
        val sourceEvent = calendarPlugin.actions.getEvent(eventId!!)
        assertThat(sourceEvent).isNotNull()
    }
}
