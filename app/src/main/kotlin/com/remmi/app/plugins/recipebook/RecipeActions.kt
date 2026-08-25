package com.remmi.app.plugins.recipebook

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.FetchIngredientMetadataCommand
import com.remmi.app.core.eventBus.events.RecipeCreatedEvent
import com.remmi.app.core.eventBus.events.RecipeDeletedEvent
import com.remmi.app.core.eventBus.events.RecipeUpdatedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.recipebook.models.RecipeItem
import com.remmi.app.plugins.ingredients.models.IngredientMetadata
import com.remmi.app.plugins.recipebook.logic.RecipeNutritionCalculator
import kotlinx.datetime.Instant

class RecipeActions(
    private val repository: RecipeRepository,
    override val id: String = "recipe_actions",
    override val name: String = "Recipe Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null

    init {
        Log.d("Remmi", "[RecipeActions] - Constructor initialized")
    }

    suspend fun getAllRecipes(): List<RecipeItem> {
        Log.d("Remmi", "[RecipeActions] - [getAllRecipes] executed")
        return try {
            repository.getAll().sortedByDescending { it.created }
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to retrieve recipes", e)
            emptyList()
        }
    }

    suspend fun sync() {
        Log.d("Remmi", "[RecipeActions] - [sync] executed")
        repository.sync()
    }

    suspend fun addRecipe(recipe: RecipeItem) {
        Log.d("Remmi", "[RecipeActions] - [addRecipe] executed")
        // Nutrition will be calculated by the AutomationEngine or a dedicated listener
        // to maintain decoupling. For now, we save and request a recalculation.
        repository.insert(recipe)
        eventBus?.publishEvent(RecipeCreatedEvent(itemId = recipe.id))
        requestNutritionRecalculation()
    }

    suspend fun updateRecipe(recipe: RecipeItem) {
        Log.d("Remmi", "[RecipeActions] - [updateRecipe] executed")
        val updatedRecipe = recipe.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
        repository.updateCloud(updatedRecipe)
        eventBus?.publishEvent(RecipeUpdatedEvent(itemId = updatedRecipe.id))
        requestNutritionRecalculation()
    }

    suspend fun deleteRecipe(id: String) {
        Log.d("Remmi", "[RecipeActions] - [deleteRecipe] executed")
        repository.delete(id)
        eventBus?.publishEvent(RecipeDeletedEvent(itemId = id))
    }

    suspend fun recalculateAllRecipes() {
        Log.d("Remmi", "[RecipeActions] - [recalculateAllRecipes] requested")
        requestNutritionRecalculation()
    }

    private suspend fun requestNutritionRecalculation() {
        Log.d("Remmi", "[RecipeActions] - Requesting global nutrition update via EventBus")
        eventBus?.publishCommand(FetchIngredientMetadataCommand())
    }

    /**
     * Called by the Plugin/Engine when metadata is available.
     */
    suspend fun performRecalculation(allMetadata: List<IngredientMetadata>) {
        val recipes = repository.getAll()
        recipes.forEach { recipe ->
            val newNutrition = RecipeNutritionCalculator.calculate(recipe, allMetadata)
            if (newNutrition != recipe.nutritionPerServing) {
                val updated = recipe.copy(nutritionPerServing = newNutrition)
                repository.updateCloud(updated)
            }
        }
    }
}
