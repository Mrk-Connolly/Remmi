package com.remmi.app.plugins.tasks

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.Relationship
import com.remmi.app.core.model.components.Reminder
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.models.Person
import com.remmi.app.core.model.models.RemmiModel
import kotlinx.datetime.Instant

/**
 * Data model representing a single task or to-do item.
 *
 * Implements [RemmiModel] for system-wide compatibility and synchronization.
 */
@Serializable
data class TaskItem(

    /**
     * Unique identifier for the task.
     */
    override val id: String,

    /**
     * Timestamp of when the task was created.
     */
    override val created: Instant,

    /**
     * Timestamp of the last time the task was modified.
     */
    override var modified: Instant,

    /**
     * The title of the task.
     */
    val title: String = "",

    /**
     * A detailed description of the task.
     */
    val description: String = "",

    /**
     * Optional starting date/time for the task.
     */
    val startingTime: Instant? = null,

    /**
     * Optional ending date/time (deadline) for the task.
     */
    val endingTime: Instant? = null,

    /**
     * The rule defining if and how the task repeats.
     */
    val repeat: RepeatRule? = null,

    /**
     * The priority level of the task.
     */
    val priority: Priority = Priority.NORMAL,

    /**
     * Whether the task has been marked as completed.
     */
    val completed: Boolean = false,

    /**
     * List of reminders for this task.
     */
    val reminders: MutableList<Reminder> = mutableListOf(),

    /**
     * List of people assigned to this task.
     */
    val assignedPeople: MutableList<Person> = mutableListOf(),

    /**
     * Optional location associated with the task.
     */
    val location: Location? = null,

    /**
     * ID of a linked calendar item, if any.
     */
    val linkedCalendarItem: String? = null,

    /**
     * Relationships with other system models.
     */
    val relationships: MutableList<Relationship> = mutableListOf()

) : RemmiModel
