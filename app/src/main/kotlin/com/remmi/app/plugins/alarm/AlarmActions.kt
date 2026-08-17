package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.core.plugins.PluginManager
import kotlin.time.Instant
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
    private val pluginManager: PluginManager,
    override val id: String = "alarm_actions",
    override val name: String = "Alarm Actions"
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal handler for Android AlarmManager */
    private val androidHandler = AndroidAlarmHandler()


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
            androidHandler.fetchSystemAlarms().map { AlarmUiModel(it, isLocal = true) }
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
        androidHandler.openSystemAlarmApp()
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
        syncToSystem: Boolean = true
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
                custom = custom
            )
            repository.insert(alarm)
            Log.d("AlarmActions", "Alarm inserted into repository: ${alarm.id}")
            
            // Schedule internal system alarm (registers as an AlarmClock in the system)
            androidHandler.setAlarm(alarm.id, alarm.title, alarm.time.toEpochMilliseconds())
            
            // Optionally push to external Clock app
            if (syncToSystem) {
                androidHandler.syncToSystemClock(alarm.title, alarm.time.toEpochMilliseconds())
            }
            
            Log.d("AlarmActions", "System alarm scheduled for: ${alarm.time}")
            
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
            alarm.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(alarm)
            
            // Reschedule internal system alarm
            Log.d("AlarmActions", "Rescheduling system alarm for: ${alarm.time}")
            androidHandler.setAlarm(alarm.id, alarm.title, alarm.time.toEpochMilliseconds())
            
            // Optionally push to external Clock app
            if (syncToSystem) {
                androidHandler.syncToSystemClock(alarm.title, alarm.time.toEpochMilliseconds())
            }
            
            true
        } catch (e: Exception) {
            Log.e("AlarmActions", "Failed to update alarm ${alarm.id}: ${e.message}", e)
            false
        }
    }

    /**                                 Delete Alarm
     * Deletes an alarm from the repository and cancels system scheduling
     */
    suspend fun deleteAlarm(id: String): Boolean {
        Log.d("Remmi", "[AlarmActions] - [deleteAlarm] executed")
        return try {
            Log.d("AlarmActions", "Deleting alarm from repository: $id")
            repository.delete(id)
            
            // Cancel system alarm
            Log.d("AlarmActions", "Canceling system alarm for: $id")
            androidHandler.cancelAlarm(id)
            
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
