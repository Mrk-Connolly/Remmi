package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.TimeRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class CalendarActions(
    private val repository: CalendarRepository
) {

    companion object {
        private const val TAG = "CalendarActions"
    }

    /* --------------------------
     * CRUD
     * -------------------------- */

    suspend fun addEvent(

        title: String,

        description: String,

        day: String,

        month: String,

        year: String,

        startTime: String,

        endTime: String,

        priority: Priority

    ): Boolean {

        return try {

            val timeZone = TimeZone.currentSystemDefault()

            val startInstant = if (startTime.isNotEmpty()) {
                try {
                    LocalDateTime.parse(
                        "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T$startTime"
                    ).toInstant(timeZone)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }

            val endInstant = if (endTime.isNotEmpty()) {
                try {
                    LocalDateTime.parse(
                        "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T$endTime"
                    ).toInstant(timeZone)
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }

            val item = CalendarItem(

                id = UUID.randomUUID().toString(),

                created = Clock.System.now(),

                modified = Clock.System.now(),

                title = title,

                description = description,

                startingTime = startInstant,
                endingTime = endInstant,
                priority = priority

            )

            repository.insert(item)

            Log.d(TAG, "Event inserted successfully")

            true

        } catch (e: Exception) {

            Log.e(TAG, "Failed to insert event", e)

            false

        }

    }

    suspend fun removeEvent(id: String): Boolean {

        return try {

            repository.delete(id)

            true

        } catch (e: Exception) {

            Log.e(TAG, "Failed to delete event", e)

            false

        }

    }

    suspend fun updateEvent(event: CalendarItem): Boolean {

        return try {

            repository.updateCloud(event)

            true

        } catch (e: Exception) {

            Log.e(TAG, "Failed to update event", e)

            false

        }

    }

    suspend fun getEvent(id: String): CalendarItem? {

        return try {

            repository.get(id)

        } catch (e: Exception) {

            Log.e(TAG, "Failed to retrieve event", e)

            null

        }

    }

    suspend fun getAllEvents(): List<CalendarItem> {

        return try {

            repository.getAll()

        } catch (e: Exception) {

            Log.e(TAG, "Failed to retrieve events", e)

            emptyList()

        }

    }

    suspend fun sync(): Boolean {

        return try {

            repository.sync()

            true

        } catch (e: Exception) {

            Log.e(TAG, "Failed to synchronize calendar", e)

            false

        }

    }

    /* --------------------------
     * Date queries
     * -------------------------- */

    suspend fun getEventsOn(date: LocalDate): List<CalendarItem> {

        return try {

            repository.getAll().filter {

                it.startingTime
                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                    ?.date == date

            }

        } catch (e: Exception) {

            Log.e(TAG, "Failed to query events for date", e)

            emptyList()

        }

    }

    suspend fun getToday(): List<CalendarItem> {

        return try {

            emptyList()

        } catch (e: Exception) {

            Log.e(TAG, "Failed to retrieve today's events", e)

            emptyList()

        }

    }

    suspend fun getUpcomingEvents(): List<CalendarItem> {

        return try {

            repository.getAll()
                .sortedBy {
                    it.startingTime ?: Instant.fromEpochMilliseconds(0)
                }

        } catch (e: Exception) {

            Log.e(TAG, "Failed to retrieve upcoming events", e)

            emptyList()

        }

    }

    /* --------------------------
     * Helpers
     * -------------------------- */

    suspend fun hasEvents(date: LocalDate): Boolean {

        return try {

            getEventsOn(date).isNotEmpty()

        } catch (e: Exception) {

            Log.e(TAG, "Failed checking events", e)

            false

        }

    }

    suspend fun eventCount(): Int {

        return try {

            repository.getAll().size

        } catch (e: Exception) {

            Log.e(TAG, "Failed counting events", e)

            0

        }

    }

}
