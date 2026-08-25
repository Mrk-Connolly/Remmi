package com.remmi.app.core.model.recipebook

import kotlinx.serialization.Serializable

@Serializable
data class StepIngredient(
    val metadataId: String,
    val name: String,
    val amount: Double,
    val unit: String // grams, volume (ml), unities, cups, spoonfuls, teaspoons
)

@Serializable
data class RecipeStep(
    val stepNumber: Int,
    val description: String,
    val ingredients: List<StepIngredient> = emptyList(),
    val approxTimeMinutes: Int = 0
)
