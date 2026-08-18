package com.remmi.app.core.plugins.model.components

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class PersonName(

    val firstName: String,
    val lastName: String? = "",
    val nickname: String? = "",
    val group: String? = ""

) {
    init {
        Log.d("Remmi", "[PersonName] - [constructor] executed")
    }
}