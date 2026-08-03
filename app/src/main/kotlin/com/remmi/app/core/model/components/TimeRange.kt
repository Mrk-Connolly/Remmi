package com.remmi.app.core.model.components

import java.time.LocalDateTime

data class TimeRange(

    val start: LocalDateTime,

    val end: LocalDateTime? = null

)