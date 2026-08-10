package com.remmi.app.core.model.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    @SerialName("LOW")
    Low,
    @SerialName("NORMAL")
    Normal,
    @SerialName("HIGH")
    High
}
