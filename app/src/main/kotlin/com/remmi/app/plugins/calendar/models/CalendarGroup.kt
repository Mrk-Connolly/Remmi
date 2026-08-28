package com.remmi.app.plugins.calendar.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CalendarGroup(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null,
    val name: String,
    @SerialName("color_hex")
    val colorHex: String,
    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,
    @SerialName("source_item_id")
    override val sourceItemId: String? = null
) : RemmiModel
