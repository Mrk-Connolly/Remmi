package com.remmi.app.testing.plugins.recipebook

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.recipebook.RecipeActions
import com.remmi.app.plugins.recipebook.models.MealType
import com.remmi.app.plugins.recipebook.models.RecipeItem
import java.util.UUID

/**
 * RECIPE ACTION TESTS
 */
class AddRecipeActionTest(
    private val actions: RecipeActions
) : RemmiActionTest {
    override val name: String = "Recipe: Add Recipe"
    override val pluginId: String = "recipe_book"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        val item = RecipeItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            title = "Diagnostic Recipe",
            description = "Created by Remmi Diagnostic System",
            mealType = MealType.OTHER
        )
        
        actions.addRecipe(item)

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_RECIPE",
            status = TestStatus.SUCCESS
        )
    }
}

/**
 * FULL FLOW: RECIPE
 */
class RecipeFullFlowActionTest(
    private val actions: RecipeActions
) : RemmiActionTest {
    override val name: String = "Recipe: Full Flow"
    override val pluginId: String = "recipe_book"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            val id = UUID.randomUUID().toString()
            val item = RecipeItem(
                id = id,
                created = now,
                modified = now,
                title = "Flow Recipe",
                description = "Testing full flow",
                mealType = MealType.LUNCH
            )
            actions.addRecipe(item)

            // 2. Get & Verify
            val recipes = actions.getAllRecipes()
            val created = recipes.find { it.id == id } 
                ?: throw IllegalStateException("Recipe not found after creation")

            // 3. Update
            val updatedItem = created.copy(title = "Updated Flow Recipe")
            actions.updateRecipe(updatedItem)

            // 4. Delete
            actions.deleteRecipe(id)

            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FULL_FLOW",
                status = TestStatus.SUCCESS
            )
        } catch (e: Exception) {
            DatabaseTestLog(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                pluginId = pluginId,
                operation = "ACTION: FULL_FLOW",
                status = TestStatus.FAILURE,
                errorMessage = e.message
            )
        }
    }
}
