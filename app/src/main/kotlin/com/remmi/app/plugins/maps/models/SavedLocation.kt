package com.remmi.app.plugins.maps.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant

@Serializable
data class SavedLocation(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    
    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String = "General",
    
    @SerialName("linked_calendar_event")
    val linkedCalendarEvent: String? = null,
    
    @SerialName("user_id")
    override val userId: String? = null,
    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,
    @SerialName("source_item_id")
    override val sourceItemId: String? = null
) : RemmiModel
