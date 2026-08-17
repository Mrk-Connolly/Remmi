package com.remmi.app.core.events

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class EventBus {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    private val _events = MutableSharedFlow<RemmiEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------


    init {
        Log.d("Remmi", "[Event Bus] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    fun stop() {
        Log.d("Remmi", "[Event Bus] - Stopping services")
    }

    fun start() {
        Log.d("Remmi", "[Event Bus] - Starting services")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------


    suspend fun publish(event: RemmiEvent) {
        Log.d("Remmi", "[EventBus] - Publishing event: ${event::class.simpleName}")
        _events.emit(event)
    }

    fun tryPublish(event: RemmiEvent) {
        Log.d("Remmi", "[EventBus] - Try publishing event: ${event::class.simpleName}")
        _events.tryEmit(event)
    }


}
