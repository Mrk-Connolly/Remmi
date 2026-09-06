package com.remmi.app.plugins.callrecorder

import android.content.Context
import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.callrecorder.models.CallRecording
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CallRecorderRepository : MemoryRepository<CallRecording>() {

    private val json = Json { ignoreUnknownKeys = true }
    private val prefName = "call_recorder_prefs"
    private val keyRecordings = "saved_recordings"
    private val keyNotification = "notification_enabled"

    fun isNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        return prefs.getBoolean(keyNotification, true)
    }

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(keyNotification, enabled).apply()
    }

    fun loadFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(keyRecordings, null)
        if (jsonStr != null) {
            try {
                val list = json.decodeFromString<List<CallRecording>>(jsonStr)
                clear()
                list.forEach { add(it) }
            } catch (e: Exception) {
                Log.e("Remmi", "[CallRecorderRepository] - Error loading recordings: ${e.message}")
            }
        }
    }

    fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val list = getAll()
        prefs.edit().putString(keyRecordings, json.encodeToString(list)).apply()
    }
}
