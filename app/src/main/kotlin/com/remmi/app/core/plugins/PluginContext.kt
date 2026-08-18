package com.remmi.app.core.plugins

import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.service.ServiceManager

/**
 * PLUGIN CONTEXT
 *
 * Shared context provided to every plugin during initialization.
 * Centralizes access to system-level managers.
 */
data class PluginContext(
    val serviceManager: ServiceManager,
    val eventBus: EventBus,
    val authRepository: AuthRepository
)
