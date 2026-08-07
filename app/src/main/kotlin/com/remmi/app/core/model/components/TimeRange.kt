package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant

@Serializable
data class TimeRange(

    val start: Instant? = null,

    val end: Instant? = null

)