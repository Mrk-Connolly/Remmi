package com.remmi.app.plugins.tasks

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.Metadata
import com.remmi.app.core.model.components.Priority
import com.remmi.app.core.model.components.Relationship
import com.remmi.app.core.model.components.Reminder
import com.remmi.app.core.model.components.TimeRange
import com.remmi.app.core.model.models.Person
import com.remmi.app.core.model.models.RemmiModel
import kotlin.time.Instant

@Serializable
data class TaskItem(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val metadata: Metadata,

    val priority: Priority = Priority.NORMAL,

    val deadline: TimeRange? = null,

    val completed: Boolean = false,

    val reminders: MutableList<Reminder> = mutableListOf(),

    val assignedPeople: MutableList<Person> = mutableListOf(),

    val location: Location? = null,

    val linkedCalendarItem: String? = null,

    val relationships: MutableList<Relationship> = mutableListOf()

) : RemmiModel