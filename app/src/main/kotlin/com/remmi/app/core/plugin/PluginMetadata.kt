package com.remmi.app.core.plugin

import android.util.Log
import kotlinx.serialization.Serializable

/**
 * PLUGIN METADATA
 * Data class containing configuration and identity information for a plugin
 */
@Serializable
data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val enabled: Boolean,
    val showInNavigation: Boolean,
    val showWidget: Boolean,
    val group: String = "personal",
    val icon: String? = null
) {

    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[PluginMetadata] - Constructor initialized")
    }
}
