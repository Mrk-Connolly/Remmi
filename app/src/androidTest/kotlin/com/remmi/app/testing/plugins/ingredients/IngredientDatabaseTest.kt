package com.remmi.app.testing.plugins.ingredients

import com.remmi.app.testing.core.*
import com.remmi.app.core.model.ingredients.*
import com.remmi.app.plugins.ingredients.repository.*
import kotlinx.datetime.*
import java.util.UUID

class IngredientDatabaseTest(
    private val metadataRepo: MetadataRepository,
    private val stockRepo: StockRepository,
    private val batchRepo: BatchRepository,
    private val testRepository: DatabaseTestRepository
) : PluginDatabaseTest {
    override val pluginId: String = "ingredient_stock"

    override suspend fun runTests(): List<DatabaseTestLog> {
        val results = mutableListOf<DatabaseTestLog>()
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        // 1. Metadata Test
        val metaTester = GenericPluginTester(pluginId + "_metadata", metadataRepo, testRepository)
        val meta = IngredientMetadata(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            name = "Test Ingredient"
        )
        results.addAll(metaTester.runCrudFlow(meta, meta.copy(name = "Updated Test Ingredient")))

        // 2. Stock Test
        val stockTester = GenericPluginTester(pluginId + "_stock", stockRepo, testRepository)
        val stock = UserStock(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            metadataId = meta.id
        )
        results.addAll(stockTester.runCrudFlow(stock, stock.copy(storageLocation = StorageLocation.FREEZER)))

        // 3. Batch Test
        val batchTester = GenericPluginTester(pluginId + "_batch", batchRepo, testRepository)
        val batch = StockBatch(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            stockId = stock.id,
            quantity = 10.0,
            purchaseDate = today
        )
        results.addAll(batchTester.runCrudFlow(batch, batch.copy(quantity = 20.0)))

        return results
    }
}
