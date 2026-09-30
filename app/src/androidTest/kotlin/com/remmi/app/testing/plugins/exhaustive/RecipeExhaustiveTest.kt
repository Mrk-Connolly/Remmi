package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.recipebook.RecipePlugin
import com.remmi.app.plugins.recipebook.models.*
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import java.util.UUID

/**
 * RECIPE EXHAUSTIVE TEST
 */
class RecipeExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testRecipeCrud_Success() = runTest {
        val plugin = controller.pluginManager.plugins["recipe_book"] as RecipePlugin
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        
        val recipe = RecipeItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Test Recipe",
            description = "A test description",
            servings = 2,
            prepTime = 10,
            cookingTime = 20,
            ovenTime = 0,
            restingTime = 0,
            totalIngredientIds = emptyList(),
            steps = emptyList(),
            ingredients = emptyList(),
            servingSize = "1 plate",
            nutritionPerServing = NutritionInfo(calories = 300.0),
            instructions = listOf("Cook it"),
            mealType = MealType.LUNCH
        )
        
        // 1. Add
        plugin.actions.addRecipe(recipe)
        var recipes = plugin.actions.getAllRecipes()
        assertThat(recipes.any { it.id == recipe.id }).isTrue()
        
        // 2. Update
        val updated = recipe.copy(title = "Updated Recipe")
        plugin.actions.updateRecipe(updated)
        recipes = plugin.actions.getAllRecipes()
        assertThat(recipes.find { it.id == recipe.id }!!.title).isEqualTo("Updated Recipe")
        
        // 3. Delete
        plugin.actions.deleteRecipe(recipe.id)
        recipes = plugin.actions.getAllRecipes()
        assertThat(recipes.any { it.id == recipe.id }).isFalse()
    }
}
