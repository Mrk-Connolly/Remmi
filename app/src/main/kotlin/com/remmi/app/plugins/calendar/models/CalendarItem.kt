package com.remmi.app.plugins.calendar.models

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
 * Aligned with the database schema in Startup.sql and user requirements.
 */
@Serializable
data class CalendarItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    // Mandatory
    val title: String,

    @SerialName("starting_date")
    val startingDate: LocalDate,

    // Mandatory but has default (in UI/Persistence)
    @SerialName("ending_date")
    val endingDate: LocalDate? = null,

    @SerialName("starting_time")
    val startingTime: LocalTime? = null,

    @SerialName("ending_time")
    val endingTime: LocalTime? = null,

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    @SerialName("group_name")
    val group: String? = null,

    @SerialName("is_repeatable")
    val isRepeatable: Boolean = false,

    // Optional
    val description: String = "",

    @SerialName("repeatable_type")
    val repeatableType: String? = null,

    @SerialName("create_alarm")
    val createAlarm: Boolean = false,

    @SerialName("participants")
    val participants: List<String> = emptyList(),

    @SerialName("create_location")
    val createLocation: Boolean = false,

    @SerialName("create_task")
    val createTask: Boolean = false,

    @SerialName("create_contact")
    val createContact: Boolean = false,

    // Infrastructure
    val repeat: List<String> = emptyList(), // Backward compatibility or complex rules

    val location: List<String> = emptyList(), // Linked location strings

    @SerialName("linked_tasks")
    val linkedTasks: List<String> = emptyList(),

    @SerialName("linked_alarm")
    val linkedAlarm: String? = null,

    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[CalendarItem] - [constructor] executed")
    }
}
