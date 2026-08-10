package com.remmi.app.plugins.alarm

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.models.RemmiModel
import kotlinx.datetime.Instant

/**
 * Data model representing an alarm.
 *
 * Aligned with the database schema in Startup.sql.
 */
@Serializable
data class AlarmItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val title: String = "",

    val description: String = "",

    val priority: Priority = Priority.Normal,

    @SerialName("linked_calendar_event")
    val linkedCalendarEvent: String? = null,

    @SerialName("linked_task")
    val linkedTask: String? = null,

    val time: Instant,

    val repeatable: List<String> = emptyList(),

    val custom: List<String> = emptyList()

) : RemmiModel
