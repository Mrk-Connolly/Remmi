package com.remmi.app.testing.plugins.ingredients

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.core.model.ingredients.FoodGroup
import com.remmi.app.core.model.ingredients.MeasurementUnit
import java.util.UUID

/**
 * INGREDIENT ACTION TESTS
 */
class AddIngredientActionTest(
    private val actions: IngredientActions
) : RemmiActionTest {
    override val name: String = "Ingredients: Add Ingredient"
    override val pluginId: String = "ingredient_stock"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        actions.addIngredient(
            name = "Diagnostic Apple",
            foodGroup = FoodGroup.FRUITS,
            initialQuantity = 5.0,
            unit = MeasurementUnit.UNITS
        )

        return DatabaseTestLog(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            pluginId = pluginId,
            operation = "ACTION: ADD_INGREDIENT",
            status = TestStatus.SUCCESS
        )
    }
}
