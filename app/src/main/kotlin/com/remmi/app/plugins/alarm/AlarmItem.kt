package com.remmi.app.plugins.alarm

import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.models.RemmiModel
import java.time.LocalDateTime
import kotlin.time.Instant

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