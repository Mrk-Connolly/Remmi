package com.remmi.app.plugins.contacts

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugins.model.models.RemmiModel
import kotlinx.datetime.Instant

/**
 * Data model representing a Contact.
 */
@Serializable
data class ContactItem(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    val name: String,
    val surname: String,
    @SerialName("mobile_phone")
    val mobilePhone: String? = null,
    val email: String? = null,
    val birthday: String? = null, // String for simplicity, e.g., "YYYY-MM-DD"

    @SerialName("group_name")
    val group: String,

    val nickname: String? = null,
    @SerialName("is_favorite")
    val isFavorite: Boolean = false,
    @SerialName("in_gift_list")
    val inGiftList: Boolean = false
) : RemmiModel {
    init {
        Log.d("Remmi", "[ContactItem] - [constructor] executed")
    }
}
