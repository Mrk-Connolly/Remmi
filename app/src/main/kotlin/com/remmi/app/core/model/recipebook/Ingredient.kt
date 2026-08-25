package com.remmi.app.core.model.recipebook

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String // Weight (g/kg), Volume (ml/l), Spoonfuls, or Count
)
