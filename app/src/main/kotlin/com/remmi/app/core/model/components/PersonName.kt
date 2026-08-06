package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class PersonName(

    val firstName: String,

    val lastName: String? = null,

    val nickname: String? = null

)