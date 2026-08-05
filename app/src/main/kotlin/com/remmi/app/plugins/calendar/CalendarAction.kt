package com.remmi.app.plugins.calendar

import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.time.Clock
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.TimeRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class CalendarActions(
    private val repository: CalendarRepository
) {

    /* --------------------------
     * CRUD
     * -------------------------- */

    fun addEvent(

        title: String,

        description: String,

        date: String,

        startTime: String,

        endTime: String,

        priority: Priority

    ) {

        val startDateTime = LocalDateTime.parse("${date}T${startTime}")
        val endDateTime = LocalDateTime.parse("${date}T${endTime}")
        val timeZone = TimeZone.currentSystemDefault()

        val item = CalendarItem(

            id = UUID.randomUUID().toString(),

            created = Clock.System.now(),

            modified = Clock.System.now(),

            title = title,

            description = description,

            time = TimeRange(
                start = startDateTime.toInstant(timeZone),
                end = endDateTime.toInstant(timeZone)
            ),

            priority = priority
        )

        repository.add(item)

    }

    fun removeEvent(id: String) {
        repository.remove(id)
    }

    fun updateEvent(event: CalendarItem) {
        repository.update(event)
    }

    fun getEvent(id: String): CalendarItem? {
        return repository.get(id)
    }

    fun getAllEvents(): List<CalendarItem> {
        return repository.getAll()
    }

    /* --------------------------
     * Date queries
     * -------------------------- */

    fun getEventsOn(date: LocalDate): List<CalendarItem> {
        return repository.getAll().filter {
            it.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date == date
        }
    }

    fun getToday(): List<CalendarItem> {
        // We'll implement this once we have a Clock service.
        return emptyList()
    }

    fun getUpcomingEvents(): List<CalendarItem> {
        return repository.getAll()
            .sortedBy { it.time.start }
    }


    /* --------------------------
     * Helpers
     * -------------------------- */

    fun hasEvents(date: LocalDate): Boolean {

        return getEventsOn(date).isNotEmpty()

    }

    fun eventCount(): Int {

        return repository.getAll().size

    }

}