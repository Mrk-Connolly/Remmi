package com.remmi.app.plugins.maps.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SavedLocation(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null,

    val name: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String = "General",

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null
) : RemmiModel
