package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.events.AlarmCreatedEvent
import com.remmi.app.core.eventBus.events.AlarmDeletedEvent
import com.remmi.app.core.eventBus.events.AlarmUpdatedEvent
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.core.android.alarms.AlarmService
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


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system event bus */
    override var eventBus: EventBus? = null

    /** Specialized Android Alarm service */
    var alarmService: AlarmService? = null


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Alarm Actions
     * */
    init {
        Log.d("Remmi", "[AlarmActions] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Get All
     * Retrieves all alarms, including local system alarms.
     */
    suspend fun getAllAlarms(): List<AlarmUiModel> {
        Log.d("Remmi", "[AlarmActions] - [getAllAlarms] executed")
        val repoAlarms = repository.getAll().map { AlarmUiModel(it, isLocal = false) }
        val systemAlarms = try {
            alarmService?.fetchSystemAlarms()?.map { AlarmUiModel(it, isLocal = true) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return (repoAlarms + systemAlarms).sortedBy { it.alarm.time }
    }
    
    /**                                 Open System App
     * Open the Android system Clock/Alarm application
     * */
    fun openSystemAlarmApp() {
        Log.d("Remmi", "[AlarmActions] - [openSystemAlarmApp] executed")
        alarmService?.openSystemAlarmApp()
    }
    
    /**                                 Add Alarm
     * Add a new alarm and schedule it in the system
     * */
    suspend fun addAlarm(
        title: String,
        description: String,
        time: Instant,
        isPriority: Boolean = false,
        repeatable: List<String> = emptyList(),
        custom: List<String> = emptyList(),
        syncToSystem: Boolean = true,
        useSound: Boolean = true,
        useVibration: Boolean = true,
        sourcePlugin: String? = null,
        sourceItemId: String? = null,
        correlationId: String? = null,
        causationId: String? = null,
        creationContext: CreationContext? = null
    ): Boolean {
        Log.d("Remmi", "[AlarmActions] - [addAlarm] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val alarm = AlarmItem(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                title = title,
                description = description,
                time = time,
                isPriority = isPriority,
                repeatable = repeatable,
                custom = custom,
                useSound = useSound,
                useVibration = useVibration,
                sourcePlugin = sourcePlugin,
                sourceItemId = sourceItemId
            )
            repository.insert(alarm)
            Log.d("AlarmActions", "Alarm inserted into repository: ${alarm.id}")
            
            // Schedule internal system alarm
            alarmService?.setAlarm(alarm.id, alarm.title, alarm.time.toEpochMilliseconds(), alarm.useSound, alarm.useVibration)
            
            // Optionally push to external Clock app
            if (syncToSystem) {
                alarmService?.syncToSystemClock(alarm.title, alarm.time.toEpochMilliseconds())
            }
            
            Log.d("AlarmActions", "System alarm scheduled for: ${alarm.time}")

            // Publish Fact
            Log.i("Remmi", "[AlarmActions] - Successfully created alarm: ${alarm.id}. Publishing event...")
            eventBus?.publishEvent(
                AlarmCreatedEvent(
                    alarmId = alarm.id,
                    correlationId = correlationId,
                    causationId = causationId,
                    creationContext = creationContext
                )
            )
            
            true
        } catch (e: Exception) {
            Log.e("AlarmActions", "Failed to add alarm: ${e.message}", e)
            false
        }
    }

    /**                                 Update Alarm
     * Updates an existing alarm and reschedules it
     */
    suspend fun updateAlarm(alarm: AlarmItem, syncToSystem: Boolean = true): Boolean {
        Log.d("Remmi", "[AlarmActions] - [updateAlarm] executed")
        return try {
            Log.d("AlarmActions", "Updating alarm in repository: ${alarm.id}")
            val updatedAlarm = alarm.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
            repository.updateCloud(updatedAlarm)
            
            // Reschedule internal system alarm
            Log.d("AlarmActions", "Rescheduling system alarm for: ${updatedAlarm.time}")
            alarmService?.setAlarm(updatedAlarm.id, updatedAlarm.title, updatedAlarm.time.toEpochMilliseconds(), updatedAlarm.useSound, updatedAlarm.useVibration)
            
            // Optionally push to external Clock app
            if (syncToSystem) {
                alarmService?.syncToSystemClock(updatedAlarm.title, updatedAlarm.time.toEpochMilliseconds())
            }

            // Publish Fact
            Log.i("Remmi", "[AlarmActions] - Successfully updated alarm: ${updatedAlarm.id}. Publishing event...")
            eventBus?.publishEvent(
                AlarmUpdatedEvent(alarmId = updatedAlarm.id)
            )
            
            true
        } catch (e: Exception) {
            Log.e("AlarmActions", "Failed to update alarm ${alarm.id}: ${e.message}", e)
            false
        }
    }

    /**                                 Delete Alarm
     * Deletes an alarm from the repository and cancels system scheduling
     */
    suspend fun deleteAlarm(
        id: String,
        correlationId: String? = null,
        causationId: String? = null,
        deletionContext: DeletionContext? = null
    ): Boolean {
        Log.d("Remmi", "[AlarmActions] - [deleteAlarm] executed")
        return try {
            val alarmToDelete = repository.get(id)
            Log.d("AlarmActions", "Deleting alarm from repository: $id")
            repository.delete(id)
            
            // Cancel system alarm
            Log.d("AlarmActions", "Canceling system alarm for: $id")
            alarmService?.cancelAlarm(id)

            // If it was synced to system clock, we try to remove it from there too if possible
            if (alarmToDelete != null) {
                alarmService?.removeFromSystemClock(alarmToDelete.title, alarmToDelete.time.toEpochMilliseconds())
            }

            // Publish Fact
            Log.i("Remmi", "[AlarmActions] - Successfully deleted alarm: $id. Publishing event...")
            eventBus?.publishEvent(
                AlarmDeletedEvent(
                    alarmId = id,
                    correlationId = correlationId,
                    causationId = causationId,
                    deletionContext = deletionContext
                )
            )
            
            true
        } catch (e: Exception) {
            Log.e("AlarmActions", "Failed to delete alarm $id: ${e.message}", e)
            false
        }
    }

    /**                                 Sync
     * Syncs alarms with cloud storage.
     */
    suspend fun sync() {
        Log.d("Remmi", "[AlarmActions] - [sync] executed")
        repository.sync()
    }

    /**                                 Get Today
     * Retrieve all alarms scheduled for today
     * */
    suspend fun getTodayAlarms(): List<AlarmItem> {
        Log.d("Remmi", "[AlarmActions] - [getTodayAlarms] executed")
        val today = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return repository.getAll().filter { 
            it.time.toLocalDateTime(TimeZone.currentSystemDefault()).date == today
        }
    }
}
