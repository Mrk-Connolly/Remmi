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
import kotlin.time.Instant

@Serializable
data class CalendarItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val title: String = "",

    val description: String = "",

    val startingTime: Instant? = null,
    val endingTime: Instant? = null,

    val priority: Priority = Priority.NORMAL,

    val participants: MutableList<Person> = mutableListOf(),

    val repeat: RepeatRule? = null,

    val reminders: MutableList<Reminder> = mutableListOf(),

    val location: Location? = null,

    val linkedTasks: MutableList<String> = mutableListOf(),

    val linkedAlarm: String? = null,

    val relationships: MutableList<Relationship> = mutableListOf()

) : RemmiModel