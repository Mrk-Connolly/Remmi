package com.remmi.app.core.plugin.model.models

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.components.ContactInfo
import com.remmi.app.core.plugin.model.components.Location
import com.remmi.app.core.plugin.model.components.PersonName
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName

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

    val notes: String = "",

    @SerialName("user_id")
    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[Person] - [constructor] executed")
    }
}
