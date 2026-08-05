package com.remmi.app.core.service

import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.services.testSupabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceManager {

    val serviceContext = SupabaseService

    fun loadPluginItems(pluginManager: PluginManager) {

    }


    fun testDBConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            testSupabase()
        }
    }

    fun close() {
        TODO("Not yet implemented")
    }
}