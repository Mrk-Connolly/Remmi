package com.remmi.app.plugins.recipebook.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: String // Weight (g/kg), Volume (ml/l), Spoonfuls, or Count
)
