package com.remmi.app.core.service

import android.content.Context
import android.util.Log

class ServiceManager(private val androidContext: Context) {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Database Service implementation */
    val databaseService: DatabaseService = SupabaseService

    // Future services can be initialized here
    // val notificationService = NotificationService(androidContext)


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Service Manager
     * */
    init {
        Log.d("Remmi", "[ServiceManager] - Initializing services")
        // Initialize static context for AndroidService if needed
        AndroidService.context = androidContext
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
