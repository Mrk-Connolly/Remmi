package com.remmi.app.plugins.tasks

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.models.RemmiModel
import kotlinx.datetime.Instant

/**
 * Data model representing a single task or to-do item.
 *
 * Aligned with the database schema in Startup.sql.
 */
@Serializable
data class TaskItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val title: String = "",

    val description: String = "",

    val completed: Boolean = false,

    @SerialName("due_date")
    val dueDate: Instant? = null,

    val priority: Priority = Priority.Normal,

    val repeat: RepeatRule? = null,

    @SerialName("parent_task")
    val parentTask: String? = null,

    @SerialName("linked_calendar")
    val linkedCalendar: String? = null,

    val reminders: List<String> = emptyList(),

    val relationships: List<String> = emptyList()

) : RemmiModel
