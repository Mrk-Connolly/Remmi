package com.remmi.app.plugins.transcriptions.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class TranscriptionGroup(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    override val userId: String? = null,
    override val sourcePlugin: String? = null,
    override val sourceItemId: String? = null,

    val name: String,
    @SerialName("color_hex")
    val colorHex: String = "#6200EE"
) : RemmiModel
