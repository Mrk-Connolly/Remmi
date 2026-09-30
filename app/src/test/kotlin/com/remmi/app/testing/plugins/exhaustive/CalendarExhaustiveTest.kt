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
open class CalendarExhaustiveTest : BasePluginActionTest() {

    @Test
    fun addEvent_validDetails_savesEventAndReturnsId() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Act
        val eventId = plugin.actions.addEvent(
            title = "Exhaustive Event",
            description = "Desc",
            startingDate = today
        )
        
        // Assert
        assertThat(eventId).isNotNull()
        val events = plugin.actions.getAllEvents()
        assertThat(events.any { it.id == eventId }).isTrue()
    }

    @Test
    fun addCalendarGroup_validNameAndColor_createsGroupSuccessfully() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        
        // Act
        val groupId = plugin.actions.addCalendarGroup("Work", "#FF0000")
        
        // Assert
        assertThat(groupId).isNotNull()
        val groups = plugin.actions.getCalendarGroups()
        assertThat(groups.any { it.name == "Work" }).isTrue()
        val groupNames = plugin.actions.getAllGroups()
        assertThat(groupNames).contains("Work")
    }

    @Test
    fun getWeeklyEvents_eventsExistWithinWeek_returnsWeeklyEvents() = runTest {
        // Arrange
        val plugin = controller.pluginManager.plugins["calendar"] as CalendarPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        plugin.actions.addEvent(title = "Weekly Event", description = "", startingDate = today.plus(2, DateTimeUnit.DAY))

        // Act
        val weekly = plugin.actions.getWeeklyEvents()

        // Assert
        assertThat(weekly.any { it.title == "Weekly Event" }).isTrue()
    }
}
