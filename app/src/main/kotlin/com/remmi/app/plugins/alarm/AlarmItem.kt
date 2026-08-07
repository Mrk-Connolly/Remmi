package com.remmi.app.plugins.alarm

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.Metadata
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.models.RemmiModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant

@Serializable
data class AlarmItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val metadata: Metadata,

    val triggerTime: LocalDateTime,

    val repeat: RepeatRule? = null,

    val enabled: Boolean = true,

    val linkedCalendarItem: String? = null,

    val linkedTaskItem: String? = null

) : RemmiModel