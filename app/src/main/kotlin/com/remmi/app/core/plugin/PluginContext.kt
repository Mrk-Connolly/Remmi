package com.remmi.app.core.plugin

import com.remmi.app.core.events.EventBus
import com.remmi.app.core.service.database.DatabaseServiceManager
import com.remmi.app.core.service.file.FileServiceManager
import com.remmi.app.core.service.android.AndroidServiceManager

/**
 * PLUGIN CONTEXT
 *
 * Shared context provided to every plugin during initialization.
 * Centralizes access to system-level managers.
 */
import com.remmi.app.core.auth.AuthRepository
data class PluginContext(
    val databaseManager: DatabaseServiceManager,
    val fileManager: FileServiceManager,
    val androidManager: AndroidServiceManager,
    val eventBus: EventBus,
    val authRepository: AuthRepository
)
