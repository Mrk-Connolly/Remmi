package com.remmi.app.core.service

import android.content.Context
import android.util.Log

class ServiceManager(private val androidContext: Context) {
    
    init {
        Log.d("Remmi", "[ServiceManager] - Initializing services")
        // Initialize static context for AndroidService if needed
        AndroidService.context = androidContext
    }

    val databaseService: DatabaseService = SupabaseService
    
    // Future services can be initialized here
    // val notificationService = NotificationService(androidContext)

    fun stop() {
        Log.d("Remmi", "[ServiceManager] - Stopping services")
    }
}
