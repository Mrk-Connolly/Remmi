package com.remmi.app.core.controller

import android.util.Log
import kotlin.reflect.KClass

/**
 * Registry and lifecycle manager for core system components.
 */
class RemmiContainer {
    private val components = mutableMapOf<KClass<*>, Any>()
    private val orderedComponents = mutableListOf<Any>()

    fun <T : Any> register(clazz: KClass<T>, instance: T) {
        components[clazz] = instance
        if (instance !in orderedComponents) {
            orderedComponents.add(instance)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: KClass<T>): T {
        return components[clazz] as? T ?: throw IllegalArgumentException("Component ${clazz.simpleName} not registered")
    }

    suspend fun startAll() {
        Log.d("Remmi", "[RemmiContainer] - Starting all components")
        orderedComponents.forEach { component ->
            if (component is RemmiComponent) {
                component.start()
            }
        }
    }

    fun stopAll() {
        Log.d("Remmi", "[RemmiContainer] - Stopping all components")
        orderedComponents.reversed().forEach { component ->
            if (component is RemmiComponent) {
                component.stop()
            }
        }
    }
}
