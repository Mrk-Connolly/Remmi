package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class PersonName(

    val firstName: String,
    val lastName: String? = "",
    val nickname: String? = "",
    val group: String? = ""

)