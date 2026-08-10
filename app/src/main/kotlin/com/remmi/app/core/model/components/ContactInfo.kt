package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class ContactInfo(

    val phone: String? = "",
    val email: String? = "",
    val email2: String? = ""

)