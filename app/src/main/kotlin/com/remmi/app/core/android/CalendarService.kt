package com.remmi.app.core.android

/**
 * CALENDAR SERVICE
 *
 * Interface for standard calendar operations.
 */
interface CalendarService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Fetch Calendar Events
     * Retrieve events from the system calendar.
     * */
    fun fetchCalendarEvents(): List<Any>

    /**                                 Create Calendar Event
     * Create a new event in the system calendar.
     * */
    fun createCalendarEvent(title: String, timeMillis: Long)
}
