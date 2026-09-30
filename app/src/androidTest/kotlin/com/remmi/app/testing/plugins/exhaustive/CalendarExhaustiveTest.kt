package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Test

/**
 * CALENDAR EXHAUSTIVE TEST
 */
class CalendarExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testAddEvent_Success() = runTest {
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        val eventId = plugin.actions.addEvent(
            title = "Exhaustive Event",
            description = "Desc",
            startingDate = today
        )
        
        assertThat(eventId).isNotNull()
        val events = plugin.actions.getAllEvents()
        assertThat(events.any { it.id == eventId }).isTrue()
    }

    @Test
    fun testCalendarGroups() = runTest {
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        
        val groupId = plugin.actions.addCalendarGroup("Work", "#FF0000")
        assertThat(groupId).isNotNull()
        
        val groups = plugin.actions.getCalendarGroups()
        assertThat(groups.any { it.name == "Work" }).isTrue()
        
        val groupNames = plugin.actions.getAllGroups()
        assertThat(groupNames).contains("Work")
    }

    @Test
    fun testGetWeeklyEvents() = runTest {
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        plugin.actions.addEvent(title = "Weekly Event", description = "", startingDate = today.plus(2, DateTimeUnit.DAY))
        val weekly = plugin.actions.getWeeklyEvents()
        assertThat(weekly.any { it.title == "Weekly Event" }).isTrue()
    }
}
