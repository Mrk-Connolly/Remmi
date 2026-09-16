package com.remmi.app.core.database.converters

import androidx.room.TypeConverter
import com.remmi.app.core.plugin.model.components.RepeatRule
import com.remmi.app.plugins.tasks.models.SubTask
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RemmiConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun fromRepeatRule(value: RepeatRule?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toRepeatRule(value: String?): RepeatRule? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromSubTaskList(value: List<SubTask>?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toSubTaskList(value: String?): List<SubTask>? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString(it) }
}
