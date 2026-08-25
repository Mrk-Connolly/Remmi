package com.remmi.app.core.model.recipebook

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.model.ingredients.AdditionalNutrient

/**
 * NUTRITION INFO (per serving)
 * 
 * Consistent with NutritionProfile used for ingredients.
 */
@Serializable
data class NutritionInfo(
    val calories: Double? = null,
    val proteins: Double? = null,
    val carbohydrates: Double? = null,
    val sugars: Double? = null,
    val fats: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,
    @SerialName("additional_nutrients")
    val additionalNutrients: List<AdditionalNutrient> = emptyList()
)
