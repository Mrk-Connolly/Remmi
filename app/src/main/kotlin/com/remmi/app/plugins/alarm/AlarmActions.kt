package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.eventBus.CreationContext
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.AlarmCreatedEvent
import com.remmi.app.core.eventBus.events.AlarmDeletedEvent
import com.remmi.app.core.eventBus.events.AlarmUpdatedEvent
import com.remmi.app.plugins.alarm.models.AlarmItem
import com.remmi.app.core.plugin.actions.RemmiAction
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

/**
 * Action controller for the Alarm plugin.
 *
 * Handles alarm scheduling, toggling, and synchronization via EventBus commands.
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
     * Retrieves all alarms from local repository.
     */
    suspend fun getAllAlarms(): List<AlarmUiModel> {
        Log.d("Remmi", "[AlarmActions] - [getAllAlarms] executed")
        return repository.getAll().map { AlarmUiModel(it, isLocal = false) }.sortedBy { it.alarm.time }
    }
    
    /**                                 Open System App
     * Open the Android system Clock/Alarm application via EventBus
     * */
    suspend fun openSystemAlarmApp() {
        Log.d("Remmi", "[AlarmActions] - [openSystemAlarmApp] executed")
        eventBus?.publishCommand(OpenSystemAlarmAppCommand())
    }
    
    /**                                 Add Alarm
     * Add a new alarm and schedule it in the system via commands
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
            
            // 1. Update local cache
            repository.add(alarm)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "alarms",
                    item = alarm,
                    serializer = AlarmItem.serializer(),
                    correlationId = correlationId,
                    causationId = causationId
                )
            )
            
            // 3. Schedule internal system alarm
            eventBus?.publishCommand(
                SetSystemAlarmCommand(
                    id = alarm.id,
                    title = alarm.title,
                    timeMillis = alarm.time.toEpochMilliseconds(),
                    useSound = alarm.useSound,
                    useVibration = alarm.useVibration,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )
            
            // 4. Optionally push to external Clock app
            if (syncToSystem) {
                eventBus?.publishCommand(
                    SyncSystemClockCommand(
                        title = alarm.title,
                        timeMillis = alarm.time.toEpochMilliseconds(),
                        correlationId = correlationId,
                        causationId = causationId
                    )
                )
            }
            
            Log.d("AlarmActions", "System alarm commands published for: ${alarm.time}")

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
     * Updates an existing alarm and reschedules it via commands
     */
    suspend fun updateAlarm(alarm: AlarmItem, syncToSystem: Boolean = true): Boolean {
        Log.d("Remmi", "[AlarmActions] - [updateAlarm] executed")
        return try {
            val updatedAlarm = alarm.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
            
            // 1. Update local cache
            repository.update(updatedAlarm)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "alarms",
                    item = updatedAlarm,
                    serializer = AlarmItem.serializer()
                )
            )
            
            // 3. Reschedule internal system alarm
            eventBus?.publishCommand(
                SetSystemAlarmCommand(
                    id = updatedAlarm.id,
                    title = updatedAlarm.title,
                    timeMillis = updatedAlarm.time.toEpochMilliseconds(),
                    useSound = updatedAlarm.useSound,
                    useVibration = updatedAlarm.useVibration
                )
            )
            
            // 4. Optionally push to external Clock app
            if (syncToSystem) {
                eventBus?.publishCommand(
                    SyncSystemClockCommand(
                        title = updatedAlarm.title,
                        timeMillis = updatedAlarm.time.toEpochMilliseconds()
                    )
                )
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
     * Deletes an alarm from the repository and cancels system scheduling via commands
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
            
            // 1. Remove from local cache
            repository.remove(id)
            
            // 2. Persist deletion to cloud
            eventBus?.publishCommand(
                DeleteDataCommand(
                    tableName = "alarms",
                    itemId = id,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )
            
            // 3. Cancel internal system alarm
            eventBus?.publishCommand(
                CancelSystemAlarmCommand(
                    id = id,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            // 4. If it was synced to system clock, try to remove it
            if (alarmToDelete != null) {
                eventBus?.publishCommand(
                    RemoveSystemClockCommand(
                        title = alarmToDelete.title,
                        timeMillis = alarmToDelete.time.toEpochMilliseconds(),
                        correlationId = correlationId,
                        causationId = causationId
                    )
                )
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
     * Requests sync from cloud via command.
     */
    suspend fun sync() {
        Log.d("Remmi", "[AlarmActions] - [sync] executed")
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "alarms",
                serializer = AlarmItem.serializer()
            )
        )
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
