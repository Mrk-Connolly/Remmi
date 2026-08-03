package com.remmi.app.core.model.components

import java.time.DayOfWeek

enum class RepeatType {

    NONE,

    DAILY,

    WEEKLY,

    MONTHLY,

    YEARLY,

    CUSTOM

}

data class RepeatRule(

    val type: RepeatType,

    val days: List<DayOfWeek> = emptyList()

)