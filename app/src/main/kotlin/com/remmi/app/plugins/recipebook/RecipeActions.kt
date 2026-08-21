package com.remmi.app.plugins.recipebook

import android.util.Log
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.actions.RemmiAction
import com.remmi.app.plugins.recipebook.models.RecipeItem

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
        repository.insert(recipe)
    }
}
