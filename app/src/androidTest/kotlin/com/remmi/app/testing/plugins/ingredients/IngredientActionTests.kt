package com.remmi.app.testing.plugins.ingredients

import com.remmi.app.testing.core.*
import com.remmi.app.plugins.ingredients.IngredientActions
import com.remmi.app.plugins.ingredients.models.FoodGroup
import com.remmi.app.plugins.ingredients.models.MeasurementUnit
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

/**
 * FULL FLOW: INGREDIENTS
 */
class IngredientFullFlowActionTest(
    private val actions: IngredientActions
) : RemmiActionTest {
    override val name: String = "Ingredients: Full Flow"
    override val pluginId: String = "ingredient_stock"

    override suspend fun execute(): DatabaseTestLog {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        return try {
            // 1. Add
            actions.addIngredient(
                name = "Flow Banana",
                foodGroup = FoodGroup.FRUITS,
                initialQuantity = 10.0,
                unit = MeasurementUnit.UNITS
            )

            // 2. Get & Verify
            val inventory = actions.getInventory()
            val created = inventory.find { it.metadata.name == "Flow Banana" } 
                ?: throw IllegalStateException("Ingredient not found after creation")

            // 3. Update Metadata
            val updatedMeta = created.metadata.copy(description = "Updated Flow Banana")
            actions.updateIngredientMetadata(updatedMeta)

            // 4. Adjust Stock (Add more)
            actions.adjustStock(created.stock.id, 5.0)
            
            // 5. Adjust Stock (Deduct)
            actions.adjustStock(created.stock.id, -2.0)

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
