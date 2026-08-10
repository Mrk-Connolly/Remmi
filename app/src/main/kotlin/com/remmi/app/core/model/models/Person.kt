package com.remmi.app.core.model.models

import kotlinx.serialization.Serializable
import com.remmi.app.core.model.components.ContactInfo
import com.remmi.app.core.model.components.Location
import com.remmi.app.core.model.components.PersonName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Data model representing a person or contact.
 */
@Serializable
data class Person(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val name: PersonName,

    val contact: ContactInfo,

    val birthday: LocalDate? = null,

    val address: Location? = null,

    val notes: String = ""

) : RemmiModel
