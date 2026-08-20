package com.remmi.app.plugins.tasks

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugins.model.components.RepeatRule
import com.remmi.app.core.plugins.model.models.RemmiModel
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

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    @SerialName("group_name")
    val group: String? = null,

    val repeat: RepeatRule? = null,

    @SerialName("parent_task")
    val parentTask: String? = null,

    @SerialName("linked_calendar")
    val linkedCalendar: String? = null,

    val reminders: List<String> = emptyList(),

    val relationships: List<String> = emptyList(),

    @SerialName("user_id")
    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[TaskItem] - [constructor] executed")
    }
}
