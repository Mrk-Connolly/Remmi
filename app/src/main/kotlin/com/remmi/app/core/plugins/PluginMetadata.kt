package com.remmi.app.core.plugins

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val enabled: Boolean,
    val showInNavigation: Boolean,
    val showWidget: Boolean,
    val icon: String? = null
) {
    init {
        Log.d("Remmi", "[PluginMetadata] - [constructor] executed")
    }
}
