package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class Metadata(

    val title: String,

    val description: String = ""

)