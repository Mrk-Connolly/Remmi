package com.remmi.app.core.events

class EventBus {

    private val listeners = mutableListOf<EventListener>()

    fun registerListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: EventListener) {
        listeners.remove(listener)
    }

    fun publish(event: RemmiEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }
}