package com.remmi.app.core.Users

import android.util.Log
import com.remmi.app.core.plugins.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing an application user.
 *
 * Aligned with the database schema in Startup.sql.
 */
@Serializable
data class User(

    override val id: String,

    override val created: Instant,

    override var modified: Instant,

    val name: String = "",

    val email: String = "",

    @SerialName("user_id")
    override val userId: String? = null

) : RemmiModel {
    init {
        Log.d("Remmi", "[User] - [constructor] executed")
    }
}