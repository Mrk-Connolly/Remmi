package com.remmi.app.plugins.gift

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugins.model.models.RemmiModel
import kotlinx.datetime.Instant

@Serializable
enum class GiftEvent {
    Christmas, Birthday, FathersDay, ValentinesDay, MothersDay, Anniversary, Other
}

@Serializable
data class GiftIdea(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("contact_id")
    val contactId: String,
    val name: String,
    val description: String? = null,
    val link: String? = null,
    val price: Double? = null,
    val event: GiftEvent? = null
) : RemmiModel {
    init {
        Log.d("Remmi", "[GiftIdea] - [constructor] executed")
    }
}
