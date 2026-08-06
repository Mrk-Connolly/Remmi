package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable
import kotlinx.datetime.DayOfWeek

@Serializable
enum class RepeatType {

    NONE,

    DAILY,

    WEEKLY,

    MONTHLY,

    YEARLY,

    CUSTOM

}

@Serializable
data class RepeatRule(

    val type: RepeatType,

    val days: List<DayOfWeek> = emptyList()

)