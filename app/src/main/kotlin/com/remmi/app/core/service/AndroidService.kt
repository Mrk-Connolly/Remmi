package com.remmi.app.core.service

import android.content.Context

interface AndroidService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE VARIABLES
    // ----------------------------------------------------------------------------

    companion object {
        var context: Context? = null
    }

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    // --- Alarm Services ---
    fun setAlarm(id: String, title: String, timeMillis: Long)
    fun cancelAlarm(id: String)
    fun syncToSystemClock(title: String, timeMillis: Long)
    fun fetchSystemAlarms(): List<com.remmi.app.plugins.alarm.AlarmItem>
    fun openSystemAlarmApp()

    // --- Notification Services ---
    fun postNotification(title: String, content: String)

    // --- Contact Services ---
    fun fetchContacts(): List<Any> // Placeholder for generic contact list
    fun createContact(name: String, phone: String)

    // --- Calendar Services ---
    fun fetchCalendarEvents(): List<Any>
    fun createCalendarEvent(title: String, timeMillis: Long)

    // --- Communication Services ---
    fun initiatePhoneCall(phoneNumber: String)
    fun sendSmsMessage(phoneNumber: String, message: String)

    // --- Location Services ---
    fun requestCurrentLocation(onLocationResult: (lat: Double, lon: Double) -> Unit)
}
