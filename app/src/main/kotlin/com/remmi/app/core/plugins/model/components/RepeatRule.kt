package com.remmi.app.core.plugins.model.components

import android.util.Log
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
        Log.d("Remmi", "[RepeatType] - [toDatabaseChar] executed")
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
            Log.d("Remmi", "[RepeatType] - [fromDatabaseChar] executed")
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
) {
    init {
        Log.d("Remmi", "[RepeatRule] - [constructor] executed")
    }
}