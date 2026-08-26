package com.remmi.app.testing.plugins

import androidx.test.platform.app.InstrumentationRegistry
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import com.remmi.app.testing.core.MockDatabaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.*
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class RelationshipIntegrationTest {

    private lateinit var controller: RemmiController

    @Before
    fun setup() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        controller = RemmiController(appContext, MockDatabaseService())
        controller.start()
    }

    @Test
    fun testCalendarToAlarmRelationship() = runTest {
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin

        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // 1. Create Calendar Event with createAlarm = true
        val eventId = calendarPlugin.actions.addEvent(
            title = "Meeting with Alarms",
            description = "Test Description",
            startingDate = today,
            startingTime = LocalTime(14, 0),
            createAlarm = true
        )
        assertNotNull(eventId)

        // 2. Wait for AlarmPlugin to react to the event
        delay(2.seconds) // Give some time for background processing

        // 3. Verify Alarm was created
        val alarms = alarmPlugin.actions.getAllAlarms()
        val linkedAlarm = alarms.find { it.alarm.sourcePlugin == "calendar" && it.alarm.sourceItemId == eventId }
        assertNotNull("Linked alarm should have been created", linkedAlarm)
        assertEquals("Alarm: Meeting with Alarms", linkedAlarm!!.alarm.title)

        // 4. Delete Calendar Event
        calendarPlugin.actions.removeEvent(eventId!!)
        
        // 5. Wait for AlarmPlugin to clean up
        delay(2.seconds)

        // 6. Verify Alarm was deleted
        val alarmsAfterDelete = alarmPlugin.actions.getAllAlarms()
        val linkedAlarmAfterDelete = alarmsAfterDelete.find { it.alarm.sourcePlugin == "calendar" && it.alarm.sourceItemId == eventId }
        assertTrue("Linked alarm should have been deleted", linkedAlarmAfterDelete == null)
    }

    @Test
    fun testCalendarToTaskRelationship() = runTest {
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val tasksPlugin = controller.pluginManager.plugins["tasks"] as TasksPlugin

        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // 1. Create Calendar Event with createTask = true
        val eventId = calendarPlugin.actions.addEvent(
            title = "Meeting with Task",
            description = "Test Task Description",
            startingDate = today,
            createTask = true
        )
        assertNotNull(eventId)

        // 2. Wait for reaction
        delay(2.seconds)

        // 3. Verify Task was created
        val tasks = tasksPlugin.actions.getAllTasks()
        val linkedTask = tasks.find { it.sourcePlugin == "calendar" && it.sourceItemId == eventId }
        assertNotNull("Linked task should have been created", linkedTask)
        assertEquals("Task for: Meeting with Task", linkedTask!!.title)

        // 4. Delete Calendar Event
        calendarPlugin.actions.removeEvent(eventId!!)
        
        // 5. Wait for cleanup
        delay(2.seconds)

        // 6. Verify Task was deleted
        val tasksAfterDelete = tasksPlugin.actions.getAllTasks()
        val linkedTaskAfterDelete = tasksAfterDelete.find { it.sourcePlugin == "calendar" && it.sourceItemId == eventId }
        assertTrue("Linked task should have been deleted", linkedTaskAfterDelete == null)
    }

    @Test
    fun testManualSecondaryDeletionDoesNotDeleteSource() = runTest {
        val calendarPlugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val alarmPlugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin

        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // 1. Create source
        val eventId = calendarPlugin.actions.addEvent(
            title = "Source Event",
            description = "Standalone Desc",
            startingDate = today,
            startingTime = LocalTime(10, 0),
            createAlarm = true
        )
        
        delay(5.seconds)
        
        val linkedAlarm = alarmPlugin.actions.getAllAlarms().find { it.alarm.sourceItemId == eventId }
        assertNotNull(linkedAlarm)

        // 2. Delete secondary manually
        alarmPlugin.actions.deleteAlarm(linkedAlarm!!.alarm.id)
        
        delay(1.seconds)

        // 3. Verify source still exists
        val sourceEvent = calendarPlugin.actions.getEvent(eventId!!)
        assertNotNull("Source event should still exist after manual alarm deletion", sourceEvent)
    }
}
