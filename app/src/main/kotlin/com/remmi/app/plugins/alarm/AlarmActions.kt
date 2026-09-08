package com.remmi.app.plugins.alarm

import android.content.Context
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import java.util.UUID

/**
 * Action controller for the Alarm plugin.
 *
 * Handles alarm scheduling and local management without a cloud database.
 */
class AlarmActions(
    private val context: Context,
    override val id: String = "alarm_actions",
    override val name: String = "Alarm Actions"
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system event bus */
    override var eventBus: EventBus? = null

    private val prefName = "remmi_alarms_local"
    private val json = Json { ignoreUnknownKeys = true }


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
     * Retrieves all alarms from local storage.
     */
    suspend fun getAllAlarms(): List<AlarmUiModel> {
        Log.d("Remmi", "[AlarmActions] - [getAllAlarms] executed")
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val data = prefs.getString("alarm_list", "[]") ?: "[]"
        return try {
            val list = json.decodeFromString(ListSerializer(AlarmItem.serializer()), data)
            list.map { AlarmUiModel(it, isLocal = true) }.sortedBy { it.alarm.time }
        } catch (e: Exception) {
            Log.e("AlarmActions", "Failed to parse local alarms", e)
            emptyList()
        }
    }
    
    private fun saveAlarms(list: List<AlarmItem>) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val data = json.encodeToString(ListSerializer(AlarmItem.serializer()), list)
        prefs.edit().putString("alarm_list", data).apply()
    }

    /**                                 Get Next System Alarm
     * Returns the next scheduled system alarm from AlarmManager.
     */
    suspend fun getNextSystemAlarm(): Long? {
        Log.d("Remmi", "[AlarmActions] - [getNextSystemAlarm] executed")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        return alarmManager.nextAlarmClock?.triggerTime
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
        skipUi: Boolean = true,
        sourcePlugin: String? = null,
        sourceItemId: String? = null,
        correlationId: String? = null,
        causationId: String? = null,
        creationContext: CreationContext? = null
    ): Boolean {
        Log.d("Remmi", "[AlarmActions] - [addAlarm] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
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
                skipUi = skipUi,
                sourcePlugin = sourcePlugin,
                sourceItemId = sourceItemId
            )
            
            // 1. Update local storage
            val current = getAllAlarms().map { it.alarm }.toMutableList()
            current.add(alarm)
            saveAlarms(current)
            
            // 2. Schedule internal system alarm
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
            
            // 3. Optionally push to external Clock app
            if (syncToSystem) {
                // Determine repeat days
                val remmiDays = if (repeatable.contains("d")) listOf(1,2,3,4,5,6,7) 
                                else if (repeatable.contains("w")) {
                                    // Map current day?
                                    listOf(now.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek.ordinal + 1)
                                }
                                else if (repeatable.contains("c")) {
                                    custom.mapNotNull { dayStr ->
                                        try { kotlinx.datetime.DayOfWeek.valueOf(dayStr.uppercase()).ordinal + 1 } catch(e: Exception) { null }
                                    }
                                } else null

                eventBus?.publishCommand(
                    SyncSystemClockCommand(
                        title = alarm.title,
                        timeMillis = alarm.time.toEpochMilliseconds(),
                        vibrate = alarm.useVibration,
                        skipUi = alarm.skipUi,
                        days = remmiDays,
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
            val updatedAlarm = alarm.copy(modified = Instant.fromEpochMilliseconds(System.currentTimeMillis()))
            
            // 1. Update local storage
            val current = getAllAlarms().map { it.alarm }.toMutableList()
            val index = current.indexOfFirst { it.id == alarm.id }
            if (index != -1) {
                current[index] = updatedAlarm
                saveAlarms(current)
            }
            
            // 2. Reschedule internal system alarm
            eventBus?.publishCommand(
                SetSystemAlarmCommand(
                    id = updatedAlarm.id,
                    title = updatedAlarm.title,
                    timeMillis = updatedAlarm.time.toEpochMilliseconds(),
                    useSound = updatedAlarm.useSound,
                    useVibration = updatedAlarm.useVibration
                )
            )
            
            // 3. Optionally push to external Clock app
            if (syncToSystem) {
                eventBus?.publishCommand(
                    SyncSystemClockCommand(
                        title = updatedAlarm.title,
                        timeMillis = updatedAlarm.time.toEpochMilliseconds(),
                        vibrate = updatedAlarm.useVibration,
                        skipUi = updatedAlarm.skipUi
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
            val alarms = getAllAlarms().map { it.alarm }
            val alarmToDelete = alarms.find { it.id == id }
            
            // 1. Remove from local storage
            val updated = alarms.filter { it.id != id }
            saveAlarms(updated)
            
            // 2. Cancel internal system alarm
            eventBus?.publishCommand(
                CancelSystemAlarmCommand(
                    id = id,
                    correlationId = correlationId,
                    causationId = causationId
                )
            )

            // 3. If it was synced to system clock, try to remove it
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
     * No longer needed as we don't sync with cloud.
     */
    suspend fun sync() {
        Log.d("Remmi", "[AlarmActions] - [sync] skipped (system only)")
    }

    /**                                 Get Today
     * Retrieve all alarms scheduled for today
     * */
    suspend fun getTodayAlarms(): List<AlarmItem> {
        Log.d("Remmi", "[AlarmActions] - [getTodayAlarms] executed")
        val today = Instant.fromEpochMilliseconds(System.currentTimeMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
        return getAllAlarms().map { it.alarm }.filter { 
            it.time.toLocalDateTime(TimeZone.currentSystemDefault()).date == today
        }
    }
}
