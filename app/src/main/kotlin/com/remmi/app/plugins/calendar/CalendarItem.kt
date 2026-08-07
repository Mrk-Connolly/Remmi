package com.remmi.app.plugins.calendar

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.Relationship
import com.remmi.app.core.model.components.Reminder
import com.remmi.app.core.model.components.RepeatRule
import com.remmi.app.core.model.components.TimeRange
import com.remmi.app.core.model.models.Person
import com.remmi.app.core.model.models.RemmiModel
import com.remmi.app.core.model.components.Metadata
import kotlinx.datetime.Instant

/**
 * Data model representing a single event in the calendar.
 *
 * Implements [RemmiModel] to ensure compatibility with the system's
 * repository and synchronization infrastructure.
 */
@Serializable
data class CalendarItem(

    /**
     * Unique identifier for the calendar event.
     */
    override val id: String,

    /**
     * Timestamp of when the event was first created.
     */
    override val created: Instant,

    /**
     * Timestamp of the last time the event was modified.
     */
    override var modified: Instant,

    /**
     * The title of the event.
     */
    val title: String = "",

    /**
     * A detailed description of the event.
     */
    val description: String = "",

    /**
     * The start time of the event.
     */
    val startingTime: Instant? = null,

    /**
     * The end time of the event.
     */
    val endingTime: Instant? = null,

    /**
     * The priority of the event (LOW, NORMAL, HIGH).
     */
    val priority: Priority = Priority.NORMAL,

    /**
     * A list of people participating in the event.
     */
    val participants: MutableList<Person> = mutableListOf(),

    /**
     * The rule defining if and how the event repeats.
     */
    val repeat: RepeatRule? = null,

    /**
     * A list of reminders scheduled for this event.
     */
    val reminders: MutableList<Reminder> = mutableListOf(),

    /**
     * The physical or virtual location of the event.
     */
    val location: Location? = null,

    /**
     * IDs of tasks linked to this calendar event.
     */
    val linkedTasks: MutableList<String> = mutableListOf(),

    /**
     * ID of an alarm linked to this calendar event.
     */
    val linkedAlarm: String? = null,

    /**
     * Relationships with other models in the system.
     */
    val relationships: MutableList<Relationship> = mutableListOf()

) : RemmiModel
