package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class ContactInfo(

    val phone: String? = null,

    val email: String? = null

)