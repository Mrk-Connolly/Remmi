package com.remmi.app.plugins.tasks.models

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.core.plugin.model.models.RemmiModel
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

    @SerialName("completed_at")
    val completedAt: Instant? = null,

    @SerialName("due_date")
    val dueDate: Instant? = null,

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    @SerialName("group_name")
    val group: String? = null,

    val subgroup: String? = null,

    val repeat: RepeatRule? = null,

    @SerialName("parent_task")
    val parentTask: String? = null,

    val reminders: List<String> = emptyList(),

    val relationships: List<String> = emptyList(),

    @SerialName("create_calendar")
    val createCalendar: Boolean = false,

    @SerialName("create_alarm")
    val createAlarm: Boolean = false,

    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[TaskItem] - [constructor] executed")
    }
}
