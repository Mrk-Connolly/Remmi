package com.remmi.app.core.events

import android.util.Log

class EventManager {

    private val listeners = mutableListOf<EventListener>()

    fun registerListener(listener: EventListener) {
        Log.d("Remmi", "[EventManager] - [registerListener] executed")
        listeners.add(listener)
    }

    fun unregisterListener(listener: EventListener) {
        Log.d("Remmi", "[EventManager] - [unregisterListener] executed")
        listeners.remove(listener)
    }

    fun publish(event: RemmiEvent) {
        Log.d("Remmi", "[EventManager] - [publish] executed")
        listeners.forEach {
            it.onEvent(event)
        }
    }
}