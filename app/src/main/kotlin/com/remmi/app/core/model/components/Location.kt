package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class Location(

    val name: String,

    val address: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null

)