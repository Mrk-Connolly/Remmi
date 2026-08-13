package com.remmi.app.core.model.components

import android.util.Log
import kotlinx.serialization.Serializable

@Serializable
data class ContactInfo(

    val phone: String? = "",
    val email: String? = "",
    val email2: String? = ""

) {
    init {
        Log.d("Remmi", "[ContactInfo] - [constructor] executed")
    }
}