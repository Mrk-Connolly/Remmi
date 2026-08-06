package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class Reminder(

    val minutesBefore: Long

)