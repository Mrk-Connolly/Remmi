package com.remmi.app.plugins.alarm

import com.remmi.app.core.actions.RemmiAction
import com.remmi.app.core.model.components.Priority
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

/**
 * Action controller for the Alarm plugin.
 *
 * Handles alarm scheduling, toggling, and synchronization.
 */
class AlarmActions(
    private val repository: AlarmRepository,
    override val id: String = "alarm_actions",
    override val name: String = "Alarm Actions"
) : RemmiAction {
    
    /**
     * Retrieves all alarms.
     */
    suspend fun getAllAlarms(): List<AlarmItem> {
        return repository.getAll().sortedBy { it.time }
    }
    
    suspend fun addAlarm(
        title: String,
        description: String,
        time: Instant,
        priority: Priority = Priority.Normal,
        repeatable: List<String> = emptyList(),
        custom: List<String> = emptyList()
    ): Boolean {
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val alarm = AlarmItem(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                title = title,
                description = description,
                time = time,
                priority = priority,
                repeatable = repeatable,
                custom = custom
            )
            repository.insert(alarm)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Updates an existing alarm.
     */
    suspend fun updateAlarm(alarm: AlarmItem): Boolean {
        return try {
            alarm.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(alarm)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes an alarm.
     */
    suspend fun deleteAlarm(id: String): Boolean {
        return try {
            repository.delete(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Syncs alarms with cloud storage.
     */
    suspend fun sync() {
        repository.sync()
    }
}
