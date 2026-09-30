package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.time.Duration.Companion.hours

/**
 * ALARM EXHAUSTIVE TEST
 */
class AlarmExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testAddAlarm_Success() = runTest {
        val plugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        val result = plugin.actions.addAlarm(
            title = "Exhaustive Test Alarm",
            description = "Description",
            time = now.plus(1.hours)
        )
        
        assertThat(result).isTrue()
        val alarms = plugin.actions.getAllAlarms()
        assertThat(alarms.any { it.alarm.title == "Exhaustive Test Alarm" }).isTrue()
    }

    @Test
    fun testUpdateAlarm_Success() = runTest {
        val plugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        plugin.actions.addAlarm(title = "Original", description = "", time = now.plus(1.hours))
        val original = plugin.actions.getAllAlarms().first().alarm
        
        val updated = original.copy(title = "Modified")
        val result = plugin.actions.updateAlarm(updated)
        
        assertThat(result).isTrue()
        assertThat(plugin.actions.getAllAlarms().first().alarm.title).isEqualTo("Modified")
    }

    @Test
    fun testGetTodayAlarms() = runTest {
        val plugin = controller.pluginManager.plugins["alarm"] as AlarmPlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        plugin.actions.addAlarm(title = "Today", description = "", time = now.plus(1.hours))
        val todayAlarms = plugin.actions.getTodayAlarms()
        assertThat(todayAlarms).isNotEmpty()
    }
}
