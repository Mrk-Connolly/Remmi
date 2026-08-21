package com.remmi.app.core.android

import com.remmi.app.plugins.alarm.AlarmItem

/**
 * ALARM SERVICE
 *
 * Interface for standard alarm operations.
 */
interface AlarmService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Set Alarm
     * Schedule a system alarm via AlarmManager.
     * */
    fun setAlarm(id: String, title: String, timeMillis: Long, useSound: Boolean = true, useVibration: Boolean = true)

    /**                                 Cancel Alarm
     * Cancel a previously scheduled system alarm.
     * */
    fun cancelAlarm(id: String)

    /**                                 Sync to System Clock
     * Push alarm details to the Android external Clock app.
     * */
    fun syncToSystemClock(title: String, timeMillis: Long)

    /**                                 Remove from System Clock
     * Attempt to remove an alarm from the external Clock app.
     * */
    fun removeFromSystemClock(title: String, timeMillis: Long)

    /**                                 Fetch System Alarms
     * Query for existing system alarms.
     * */
    fun fetchSystemAlarms(): List<AlarmItem>

    /**                                 Open System App
     * Show the system alarms interface.
     * */
    fun openSystemAlarmApp()
}
