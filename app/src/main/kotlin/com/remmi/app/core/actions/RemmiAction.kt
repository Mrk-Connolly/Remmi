package com.remmi.app.core.actions

import com.remmi.app.core.RemmiClass

/**
 * Base interface for all actions within the Remmi ecosystem.
 *
 * This interface serves two purposes:
 * 1. As a base for individual atomic action commands that implement [execute].
 * 2. As a base for plugin-level action controllers that manage multiple related operations.
 */
interface RemmiAction : RemmiClass {

    /**
     * Unique identifier for the action.
     */
    val id: String get() = ""

    /**
     * Display name of the action.
     */
    val name: String get() = ""

    /**
     * Executes the primary operation of this action.
     * Plugin-level controllers may not implement this if they provide specific methods instead.
     */
    fun execute(): ActionResult = ActionResult()
}
