package com.remmi.app.core.model.alarm

import android.util.Log
import kotlinx.serialization.Serializable
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant

/**
 * Data model representing a single alarm or notification reminder.
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

    val isPriority: Boolean = false,

    val linkedCalendarEvent: String? = null,

    val linkedTask: String? = null,

    val time: Instant,

    val repeatable: List<String> = emptyList(),

    val custom: List<String> = emptyList(),

    val useSound: Boolean = true,

    val useVibration: Boolean = true,

    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[AlarmItem] - [constructor] executed")
    }
}
