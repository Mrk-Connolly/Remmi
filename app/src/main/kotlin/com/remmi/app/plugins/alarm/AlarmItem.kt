package com.remmi.app.plugins.alarm

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
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

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    @SerialName("linked_calendar_event")
    val linkedCalendarEvent: String? = null,

    @SerialName("linked_task")
    val linkedTask: String? = null,

    val time: Instant,

    val repeatable: List<String> = emptyList(),

    val custom: List<String> = emptyList(),

    @SerialName("use_sound")
    val useSound: Boolean = true,

    @SerialName("use_vibration")
    val useVibration: Boolean = true,

    @SerialName("user_id")
    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[AlarmItem] - [constructor] executed")
    }
}
