package com.remmi.app

import android.app.Application
import android.util.Log
import com.remmi.app.core.host.RemmiHost

/**
 * REMMI APPLICATION
 * 
 * Global entry point for the application.
 * Manages the singleton instance of RemmiHost to ensure consistency
 * between UI and background components (like Widgets).
 */
class RemmiApplication : Application() {

    lateinit var remmiHost: RemmiHost
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("Remmi", "[RemmiApplication] - Created")
        remmiHost = RemmiHost(this)
    }
}
