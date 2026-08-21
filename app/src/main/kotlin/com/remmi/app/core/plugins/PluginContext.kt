package com.remmi.app.core.plugins

import com.remmi.app.core.events.EventBus
import com.remmi.app.core.database.DatabaseServiceManager
import com.remmi.app.core.file.FileServiceManager
import com.remmi.app.core.android.AndroidServiceManager

/**
 * PLUGIN CONTEXT
 *
 * Shared context provided to every plugin during initialization.
 * Centralizes access to system-level managers.
 */
data class PluginContext(
    val databaseManager: DatabaseServiceManager,
    val fileManager: FileServiceManager,
    val androidManager: AndroidServiceManager,
    val eventBus: EventBus
)
