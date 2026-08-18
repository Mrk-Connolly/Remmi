package com.remmi.app.core.service

import android.content.Context
import android.util.Log
import com.remmi.app.core.events.CommandListener
import com.remmi.app.core.events.RemmiCommand
import com.remmi.app.core.events.SaveDataCommand
import com.remmi.app.core.service.android.*
import com.remmi.app.core.service.android.implementations.AndroidAlarmService
import com.remmi.app.core.service.android.implementations.AndroidWeatherService
import com.remmi.app.core.service.database.DatabaseService
import com.remmi.app.core.service.database.SupabaseService
import com.remmi.app.core.service.file.AndroidFileService
import com.remmi.app.core.service.file.FileService

/**
 * SERVICE MANAGER
 *
 * Centralized registry for all system services and router for system Commands.
 */
class ServiceManager(private val androidContext: Context) : CommandListener {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Database Service implementation */
    val databaseService: DatabaseService = SupabaseService

    /** File Service implementation */
    val fileService: FileService = AndroidFileService(androidContext)

    /** Specialized Android Services */
    val alarmService: AlarmService = AndroidAlarmService(androidContext)
    val notificationService: NotificationService = AndroidAlarmService(androidContext)
    val weatherService: WeatherService = AndroidWeatherService()


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[ServiceManager] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Start all system services.
     * */
    fun start() {
        Log.d("Remmi", "[ServiceManager] - Starting services")
    }

    /**                                 Stop
     * Stop all system services.
     * */
    fun stop() {
        Log.d("Remmi", "[ServiceManager] - Stopping services")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Command
     * Handle incoming Intents targeted at system services.
     * */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.i("Remmi", "[ServiceManager] - RECEIVED COMMAND: [${command::class.simpleName}] from [${command.source}]")
        
        when (command) {
            is SaveDataCommand -> {
                Log.i("Remmi", "[ServiceManager] - Executing global save requested by ${command.source}")
                // TODO: Trigger Supabase sync or local DB backup
            }
        }
    }
}
