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
