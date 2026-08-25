package com.remmi.app.core.android.services

import android.content.Context

/**
 * SYSTEM SETTINGS SERVICE
 *
 * Wrapper for Android SharedPreferences to restrict direct Context access.
 */
class SystemSettingsService(context: Context) {
    
    private val sharedPrefs = context.getSharedPreferences("remmi_settings", Context.MODE_PRIVATE)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean = sharedPrefs.getBoolean(key, defaultValue)
    fun setBoolean(key: String, value: Boolean) = sharedPrefs.edit().putBoolean(key, value).apply()

    fun getInt(key: String, defaultValue: Int): Int = sharedPrefs.getInt(key, defaultValue)
    fun setInt(key: String, value: Int) = sharedPrefs.edit().putInt(key, value).apply()

    fun getString(key: String, defaultValue: String?): String? = sharedPrefs.getString(key, defaultValue)
    fun setString(key: String, value: String?) = sharedPrefs.edit().putString(key, value).apply()
}
