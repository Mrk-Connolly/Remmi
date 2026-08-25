package com.remmi.app.plugins.ingredients.logic

import com.remmi.app.core.model.ingredients.*

/**
 * INGREDIENT ENRICHMENT SERVICE
 * 
 * Logic for fetching nutritional information and metadata from external sources or AI.
 */
interface IngredientEnrichmentService {

    suspend fun searchIngredient(query: String): IngredientEnrichmentResult
}

data class IngredientEnrichmentResult(
    val canonicalName: String,
    val foodGroup: FoodGroup,
    val nutritionPer100g: NutritionProfile,
    val estimatedShelfLifeMinDays: Int? = null,
    val estimatedShelfLifeMaxDays: Int? = null,
    val source: String? = null,
    val confidence: Double = 1.0
)

/**
 * DEFAULT IMPLEMENTATION
 * 
 * Provides mock data for demonstration. In a production environment, 
 * this would call food databases (e.g., USDA, Edamam) or an LLM.
 */
class DefaultIngredientEnrichmentService : IngredientEnrichmentService {
    
    override suspend fun searchIngredient(query: String): IngredientEnrichmentResult {
        // Mock data logic based on common queries
        return when (query.lowercase()) {
            "carrot", "carrots" -> IngredientEnrichmentResult(
                canonicalName = "Carrots",
                foodGroup = FoodGroup.VEGETABLES,
                nutritionPer100g = NutritionProfile(
                    calories = 41.0,
                    proteins = 0.9,
                    carbohydrates = 9.6,
                    sugars = 4.7,
                    fats = 0.2,
                    fiber = 2.8,
                    sodium = 69.0,
                    additionalNutrients = listOf(
                        AdditionalNutrient("Vitamin A", 835.0, "µg"),
                        AdditionalNutrient("Potassium", 320.0, "mg")
                    )
                ),
                estimatedShelfLifeMinDays = 14,
                estimatedShelfLifeMaxDays = 21,
                source = "System Reference"
            )
            "apple", "apples" -> IngredientEnrichmentResult(
                canonicalName = "Apples",
                foodGroup = FoodGroup.FRUITS,
                nutritionPer100g = NutritionProfile(
                    calories = 52.0,
                    proteins = 0.3,
                    carbohydrates = 13.8,
                    sugars = 10.4,
                    fats = 0.2,
                    fiber = 2.4,
                    sodium = 1.0
                ),
                estimatedShelfLifeMinDays = 7,
                estimatedShelfLifeMaxDays = 30,
                source = "System Reference"
            )
            else -> IngredientEnrichmentResult(
                canonicalName = query.replaceFirstChar { it.uppercase() },
                foodGroup = FoodGroup.OTHER,
                nutritionPer100g = NutritionProfile(),
                source = "Search Failed"
            )
        }
    }
}
