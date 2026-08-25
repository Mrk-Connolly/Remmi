package com.remmi.app.core.plugin

import com.remmi.app.core.events.EventBus
import com.remmi.app.core.service.database.DatabaseManager
import com.remmi.app.core.service.file.FileManager
import com.remmi.app.core.service.android.AndroidServiceManager

/**
 * PLUGIN CONTEXT
 *
 * Shared context provided to every plugin during initialization.
 * Centralizes access to system-level managers.
 */
data class PluginContext(
    val databaseManager: DatabaseManager,
    val fileManager: FileManager,
    val androidManager: AndroidServiceManager,
    val eventBus: EventBus
)
