package com.remmi.app.core.model.models

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.ContactInfo
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.PersonName
import com.remmi.app.core.model.components.Relationship
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Serializable
data class Person(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val name: PersonName,

    val contact: ContactInfo,

    val birthday: LocalDate? = null,

    val address: Location? = null,

    val notes: String = "",

    val relationships: MutableList<Relationship> = mutableListOf()

) : RemmiModel