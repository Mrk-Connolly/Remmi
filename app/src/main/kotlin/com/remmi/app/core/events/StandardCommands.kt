package com.remmi.app.core.events

import java.util.UUID

/**
 * DELETE ALARM COMMAND
 * Request to delete a specific alarm by its ID.
 */
data class DeleteAlarmCommand(
    val alarmId: String,
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "automation"
) : RemmiCommand

/**
 * SAVE DATA COMMAND
 * Request to perform a global data save.
 */
data class SaveDataCommand(
    override val commandId: String = UUID.randomUUID().toString(),
    override val source: String = "system"
) : RemmiCommand
