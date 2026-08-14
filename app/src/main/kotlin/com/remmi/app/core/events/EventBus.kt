package com.remmi.app.core.events

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class EventBus {
    private val _events = MutableSharedFlow<RemmiEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    suspend fun publish(event: RemmiEvent) {
        Log.d("Remmi", "[EventBus] - Publishing event: ${event::class.simpleName}")
        _events.emit(event)
    }

    fun tryPublish(event: RemmiEvent) {
        Log.d("Remmi", "[EventBus] - Try publishing event: ${event::class.simpleName}")
        _events.tryEmit(event)
    }
}
