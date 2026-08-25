package com.remmi.app.plugins.recipebook.logic

import com.remmi.app.core.model.ingredients.*
import com.remmi.app.core.model.recipebook.NutritionInfo
import com.remmi.app.core.model.recipebook.RecipeItem
import com.remmi.app.core.model.recipebook.StepIngredient

/**
 * RECIPE NUTRITION CALCULATOR
 * 
 * Logic for aggregating nutritional values from ingredients.
 */
object RecipeNutritionCalculator {

    /**
     * Calculate per-serving nutrition for a recipe.
     */
    fun calculate(
        recipe: RecipeItem,
        allMetadata: List<IngredientMetadata>
    ): NutritionInfo {
        if (recipe.servings <= 0) return NutritionInfo()

        var totalCalories = 0.0
        var totalProteins = 0.0
        var totalCarbs = 0.0
        var totalSugars = 0.0
        var totalFats = 0.0
        var totalFiber = 0.0
        var totalSodium = 0.0
        val additionalMap = mutableMapOf<String, Pair<Double, String>>() // Name to (Total Value, Unit)

        // Flatten all ingredients from steps
        val stepIngredients = recipe.steps.flatMap { it.ingredients }
        
        // Also consider the top-level ingredients list if it's the primary source
        val allIngredients = if (stepIngredients.isNotEmpty()) stepIngredients else {
            recipe.ingredients.map { ing ->
                // Map legacy Ingredient to StepIngredient if needed, or handle separately
                StepIngredient(
                    metadataId = "", // Legacy ingredients might not have IDs
                    name = ing.name,
                    amount = ing.amount,
                    unit = ing.unit
                )
            }
        }

        allIngredients.forEach { ingredient ->
            val metadata = allMetadata.find { 
                it.id == ingredient.metadataId || it.name.equals(ingredient.name, ignoreCase = true) 
            } ?: return@forEach

            val nutrition = metadata.baseNutrition ?: return@forEach
            val basis = metadata.nutritionBasis ?: NutritionBasis(100.0, MeasurementUnit.GRAMS)
            
            // 1. Convert ingredient amount to the basis unit (usually GRAMS or MILLILITERS)
            val ingredientUnit = try {
                MeasurementUnit.valueOf(ingredient.unit.uppercase())
            } catch (e: Exception) {
                MeasurementUnit.GRAMS // Default to grams if unknown
            }
            
            val convertedAmount = convertUnit(ingredient.amount, ingredientUnit, basis.unit, metadata)
            
            // 2. Calculate the multiplier (how many nutrition blocks are in this amount)
            val multiplier = if (basis.amount > 0) convertedAmount / basis.amount else 0.0

            // 3. Add to totals
            totalCalories += (nutrition.calories ?: 0.0) * multiplier
            totalProteins += (nutrition.proteins ?: 0.0) * multiplier
            totalCarbs += (nutrition.carbohydrates ?: 0.0) * multiplier
            totalSugars += (nutrition.sugars ?: 0.0) * multiplier
            totalFats += (nutrition.fats ?: 0.0) * multiplier
            totalFiber += (nutrition.fiber ?: 0.0) * multiplier
            totalSodium += (nutrition.sodium ?: 0.0) * multiplier

            // Additional nutrients
            nutrition.additionalNutrients.forEach { nut ->
                val existing = additionalMap[nut.name]
                if (existing != null) {
                    additionalMap[nut.name] = (existing.first + (nut.value * multiplier)) to nut.unit
                } else {
                    additionalMap[nut.name] = (nut.value * multiplier) to nut.unit
                }
            }
        }

        // 4. Divide by servings
        val servings = recipe.servings.toDouble()
        return NutritionInfo(
            calories = totalCalories / servings,
            proteins = totalProteins / servings,
            carbohydrates = totalCarbs / servings,
            sugars = totalSugars / servings,
            fats = totalFats / servings,
            fiber = totalFiber / servings,
            sodium = totalSodium / servings,
            additionalNutrients = additionalMap.map { (name, pair) ->
                AdditionalNutrient(name, pair.first / servings, pair.second)
            }
        )
    }
}
