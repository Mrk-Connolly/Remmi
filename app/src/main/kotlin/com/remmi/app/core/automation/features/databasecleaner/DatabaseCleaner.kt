package com.remmi.app.core.automation.features.databasecleaner

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.DeleteTaskCommand
import com.remmi.app.core.eventBus.DeletionContext
import com.remmi.app.plugins.tasks.models.TaskItem
import kotlinx.datetime.*
import java.util.UUID

/**
 * DATABASE CLEANER
 * 
 * Automation feature to remove old completed tasks and other expired data.
 */
class DatabaseCleaner(private val eventBus: EventBus) {

    /**
     * Searches for tasks completed more than 7 days ago and deletes them.
     */
    suspend fun cleanTaskDatabase(tasks: List<TaskItem>) {
        Log.i("Remmi", "[DatabaseCleaner] - Starting Task Database cleanup")
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val oneWeekAgo = now.minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

        val oldCompletedTasks = tasks.filter { 
            it.completed && it.completedAt != null && it.completedAt < oneWeekAgo 
        }

        Log.d("Remmi", "[DatabaseCleaner] - Found ${oldCompletedTasks.size} tasks to delete")

        oldCompletedTasks.forEach { task ->
            Log.d("Remmi", "[DatabaseCleaner] - Deleting old task: ${task.id} (Title: ${task.title})")
            eventBus.publishCommand(
                DeleteTaskCommand(
                    taskId = task.id,
                    source = "database_cleaner",
                    deletionContext = DeletionContext.LINKED_CLEANUP
                )
            )
        }
    }
}
