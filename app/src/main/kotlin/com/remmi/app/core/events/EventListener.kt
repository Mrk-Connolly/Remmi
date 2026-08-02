package com.remmi.app.core.events

interface EventListener {

    fun onEvent(event: RemmiEvent)

}