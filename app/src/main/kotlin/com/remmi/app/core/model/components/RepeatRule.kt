package com.remmi.app.core.model.components

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
enum class RepeatType {

    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM;

    fun toDatabaseChar(): String {
        return when (this) {
            NONE -> "n"
            DAILY -> "d"
            WEEKLY -> "w"
            MONTHLY -> "m"
            YEARLY -> "y"
            CUSTOM -> "c"
        }
    }

    companion object {

        fun fromDatabaseChar(value: String): RepeatType {
            return when (value.lowercase()) {
                "n" -> NONE
                "d" -> DAILY
                "w" -> WEEKLY
                "m" -> MONTHLY
                "y" -> YEARLY
                "c" -> CUSTOM

                else -> throw IllegalArgumentException(
                    "Unknown repeat type: $value"
                )
            }
        }
    }
}

@Serializable
data class RepeatRule(
    val type: RepeatType,
    val days: List<DayOfWeek> = emptyList()
)