package com.remmi.app.core.service

import android.content.Context
import android.util.Log
import com.remmi.app.core.service.android.*
import com.remmi.app.core.service.android.implementations.AndroidAlarmService
import com.remmi.app.core.service.database.DatabaseService
import com.remmi.app.core.service.database.SupabaseService
import com.remmi.app.core.service.file.AndroidFileService
import com.remmi.app.core.service.file.FileService

/**
 * SERVICE MANAGER
 *
 * Centralized registry for all system services.
 * Provides access to database, file system, and specialized Android services.
 */
class ServiceManager(private val androidContext: Context) {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Database Service implementation */
    val databaseService: DatabaseService = SupabaseService

    /** File Service implementation */
    val fileService: FileService = AndroidFileService(androidContext)

    /** Specialized Android Services */
    val alarmService: AlarmService = AndroidAlarmService(androidContext)
    val notificationService: NotificationService = AndroidAlarmService(androidContext) // Shared implementation for now
    
    // Future specialized services
    // val contactService: ContactService = AndroidContactService(androidContext)
    // val calendarService: CalendarService = AndroidCalendarService(androidContext)


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Service Manager
     * */
    init {
        Log.d("Remmi", "[ServiceManager] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Start all system services
     * */
    fun start() {
        Log.d("Remmi", "[ServiceManager] - Starting services")
    }

    /**                                 Stop
     * Stop all system services
     * */
    fun stop() {
        Log.d("Remmi", "[ServiceManager] - Stopping services")
    }

}
