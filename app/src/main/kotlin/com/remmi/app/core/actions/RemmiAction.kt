package com.remmi.app.core.actions

interface RemmiAction {

    val id: String
    val name: String

    fun execute(): ActionResult
}