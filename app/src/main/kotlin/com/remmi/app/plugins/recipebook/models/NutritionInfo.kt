package com.remmi.app.plugins.recipebook.models

import kotlinx.serialization.Serializable

@Serializable
data class NutritionInfo(
    val calories: Double = 0.0,
    val proteins: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val sugars: Double = 0.0,
    val fats: Double = 0.0,
    val fiber: Double = 0.0,
    val vitamins: Map<String, String> = emptyMap() // Name to amount (e.g. "Vitamin C" to "20mg")
)
