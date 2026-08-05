package com.remmi.app.core.model.components

import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

data class TimeRange(

    val start: Instant,

    val end: Instant? = null

)