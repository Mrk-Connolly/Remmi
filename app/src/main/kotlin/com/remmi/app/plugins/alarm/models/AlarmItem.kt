package com.remmi.app.plugins.alarm.models

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
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

    @SerialName("is_priority")
    val isPriority: Boolean = false,

    val time: Instant,

    val repeatable: List<String> = emptyList(),

    val custom: List<String> = emptyList(),

    @SerialName("use_sound")
    val useSound: Boolean = true,

    @SerialName("use_vibration")
    val useVibration: Boolean = true,

    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[AlarmItem] - [constructor] executed")
    }
}
