package com.remmi.app.plugins.ingredients

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.events.IngredientCreatedEvent
import com.remmi.app.core.eventBus.events.IngredientUpdatedEvent
import com.remmi.app.core.eventBus.events.IngredientStockAdjustedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.ingredients.models.*
import com.remmi.app.plugins.ingredients.repository.*
import kotlinx.datetime.*
import java.util.UUID

class IngredientActions(
    private val metadataRepo: MetadataRepository,
    private val stockRepo: StockRepository,
    private val batchRepo: BatchRepository,
    override val id: String = "ingredient_actions",
    override val name: String = "Ingredient Actions"
) : RemmiAction {

    override var eventBus: EventBus? = null

    init {
        Log.d("Remmi", "[IngredientActions] - Constructor initialized")
    }

    /**
     * Get all inventory items for the UI
     */
    suspend fun getInventory(): List<IngredientUiModel> {
        val metadata = metadataRepo.getAll()
        val stock = stockRepo.getAll()
        val batches = batchRepo.getAll()

        return stock.mapNotNull { userStock ->
            val meta = metadata.find { it.id == userStock.metadataId } ?: return@mapNotNull null
            val itemBatches = batches.filter { it.stockId == userStock.id }
            IngredientUiModel(meta, userStock, itemBatches)
        }
    }

    suspend fun getMetadataList(): List<IngredientMetadata> {
        return metadataRepo.getAll()
    }

    /**
     * Add a new ingredient and initial stock
     */
    suspend fun addIngredient(
        name: String,
        foodGroup: FoodGroup,
        initialQuantity: Double,
        unit: MeasurementUnit,
        expiryDate: LocalDate? = null,
        brand: String? = null,
        description: String = "",
        storageLocation: StorageLocation = StorageLocation.PANTRY,
        allowedUnits: List<MeasurementUnit> = emptyList(),
        conversions: List<IngredientConversion> = emptyList(),
        baseNutrition: NutritionProfile? = null,
        shelfLife: Pair<Int?, Int?>? = null
    ) {
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        
        // 1. Create Metadata
        val meta = IngredientMetadata(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            userId = null,
            name = name,
            foodGroup = foodGroup,
            brand = brand,
            description = description,
            allowedUnits = allowedUnits,
            conversions = conversions,
            baseNutrition = baseNutrition,
            estimatedShelfLifeMinDays = shelfLife?.first,
            estimatedShelfLifeMaxDays = shelfLife?.second
        )
        metadataRepo.insert(meta)

        // 2. Create User Stock Link
        val stock = UserStock(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            userId = null,
            metadataId = meta.id,
            primaryUnit = unit,
            storageLocation = storageLocation
        )
        stockRepo.insert(stock)

        // 3. Create Initial Batch
        if (initialQuantity > 0) {
            val batch = StockBatch(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                userId = null,
                stockId = stock.id,
                quantity = initialQuantity,
                purchaseDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                expiryDate = expiryDate
            )
            batchRepo.insert(batch)
        }

        eventBus?.publishEvent(IngredientCreatedEvent(itemId = meta.id))
    }

    /**
     * Update ingredient metadata
     */
    suspend fun updateIngredientMetadata(metadata: IngredientMetadata) {
        Log.d("Remmi", "[IngredientActions] - Updating metadata for ${metadata.id}")
        metadata.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        metadataRepo.updateCloud(metadata)
        eventBus?.publishEvent(IngredientUpdatedEvent(itemId = metadata.id))
    }

    /**
     * Adjust stock quantity. 
     */
    suspend fun adjustStock(stockId: String, delta: Double, expiryDate: LocalDate? = null) {
        if (delta == 0.0) return
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())

        if (delta > 0) {
            // Increase: Create new batch
            val batch = StockBatch(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                userId = null,
                stockId = stockId,
                quantity = delta,
                purchaseDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                expiryDate = expiryDate
            )
            batchRepo.insert(batch)
            eventBus?.publishEvent(IngredientStockAdjustedEvent(itemId = stockId, delta = delta))
        } else {
            // Decrease: FEFO Logic
            var remainingToDeduct = -delta
            val activeBatches = batchRepo.getAll()
                .filter { it.stockId == stockId && it.quantity > 0 }
                .sortedWith(compareBy<StockBatch> { it.expiryDate ?: LocalDate(9999, 12, 31) }.thenBy { it.purchaseDate })

            for (batch in activeBatches) {
                if (remainingToDeduct <= 0) break
                
                if (batch.quantity <= remainingToDeduct) {
                    remainingToDeduct -= batch.quantity
                    batchRepo.delete(batch.id)
                } else {
                    val updatedBatch = batch.copy(
                        quantity = batch.quantity - remainingToDeduct,
                        modified = now
                    )
                    batchRepo.updateCloud(updatedBatch)
                    remainingToDeduct = 0.0
                }
            }
            eventBus?.publishEvent(IngredientStockAdjustedEvent(itemId = stockId, delta = delta))
        }
    }

    suspend fun sync() {
        metadataRepo.sync()
        stockRepo.sync()
        batchRepo.sync()
    }
}
