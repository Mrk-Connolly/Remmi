package com.remmi.app.testing.plugins.exhaustive

import com.google.common.truth.Truth.assertThat
import com.remmi.app.plugins.ingredients.IngredientPlugin
import com.remmi.app.plugins.ingredients.models.FoodGroup
import com.remmi.app.plugins.ingredients.models.MeasurementUnit
import com.remmi.app.testing.base.BasePluginActionTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * INGREDIENT EXHAUSTIVE TEST
 */
class IngredientExhaustiveTest : BasePluginActionTest() {

    @Test
    fun testIngredientFullFlow_Success() = runTest {
        val plugin = controller.pluginManager.plugins["ingredient_stock"] as IngredientPlugin
        
        // 1. Add Shop
        val shop = plugin.actions.addShop("Test Market", "123 Main St")
        assertThat(shop.name).isEqualTo("Test Market")
        
        // 2. Add Ingredient
        plugin.actions.addIngredient(
            name = "Milk",
            foodGroup = FoodGroup.OTHER,
            initialQuantity = 2.0,
            unit = MeasurementUnit.LITERS,
            shopId = shop.id
        )
        
        var inventory = plugin.actions.getInventory()
        var milk = inventory.find { it.metadata.name == "Milk" }
        assertThat(milk).isNotNull()
        assertThat(milk!!.stock.primaryUnit).isEqualTo(MeasurementUnit.LITERS)
        assertThat(milk.batches).hasSize(1)
        assertThat(milk.batches[0].quantity).isEqualTo(2.0)
        
        // 3. Adjust Stock (Increase)
        plugin.actions.adjustStock(milk.stock.id, 1.5)
        inventory = plugin.actions.getInventory()
        milk = inventory.find { it.stock.id == milk.stock.id }
        assertThat(milk!!.batches).hasSize(2)
        
        // 4. Adjust Stock (Decrease - FEFO)
        plugin.actions.adjustStock(milk.stock.id, -1.0)
        inventory = plugin.actions.getInventory()
        milk = inventory.find { it.stock.id == milk!!.stock.id }
        // First batch was 2.0, now should be 1.0
        val firstBatch = milk!!.batches.find { it.quantity == 1.0 }
        assertThat(firstBatch).isNotNull()
    }
}
