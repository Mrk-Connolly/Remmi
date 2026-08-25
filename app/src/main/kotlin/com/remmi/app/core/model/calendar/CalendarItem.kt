package com.remmi.app.core.model.calendar

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Data model representing a single event in the calendar.
 *
 * Aligned with the database schema in Startup.sql.
 */
@Serializable
data class CalendarItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val title: String = "",

    val description: String = "",

    @SerialName("starting_date")
    val startingDate: LocalDate,

    @SerialName("starting_time")
    val startingTime: LocalTime? = null,

    @SerialName("ending_date")
    val endingDate: LocalDate? = null,

    @SerialName("ending_time")
    val endingTime: LocalTime? = null,

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    @SerialName("group_name")
    val group: String? = null,

    val participants: List<String> = emptyList(),

    val repeat: List<String> = emptyList(),

    val location: List<String> = emptyList(),

    @SerialName("linked_tasks")
    val linkedTasks: List<String> = emptyList(),

    @SerialName("linked_alarm")
    val linkedAlarm: String? = null,

    @SerialName("user_id")
    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[CalendarItem] - [constructor] executed")
    }
}
