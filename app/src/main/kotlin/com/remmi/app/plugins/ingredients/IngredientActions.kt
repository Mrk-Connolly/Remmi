package com.remmi.app.plugins.ingredients

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.IngredientCreatedEvent
import com.remmi.app.core.eventBus.events.IngredientUpdatedEvent
import com.remmi.app.core.eventBus.events.IngredientStockAdjustedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.ingredients.logic.IngredientReceiptMatcher
import com.remmi.app.plugins.ingredients.logic.ReceiptParser
import com.remmi.app.plugins.ingredients.models.*
import kotlinx.datetime.*
import java.util.UUID

/**
 * Action controller for the Ingredient plugin via EventBus.
 */
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
     * Get all inventory items for the UI from cache.
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
     * Add a new ingredient and initial stock via commands.
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
        metadataRepo.add(meta)
        eventBus?.publishCommand(UpsertDataCommand(tableName = "ingredient_metadata", item = meta, serializer = IngredientMetadata.serializer()))

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
        stockRepo.add(stock)
        eventBus?.publishCommand(UpsertDataCommand(tableName = "user_stock", item = stock, serializer = UserStock.serializer()))

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
            batchRepo.add(batch)
            eventBus?.publishCommand(UpsertDataCommand(tableName = "stock_batches", item = batch, serializer = StockBatch.serializer()))
        }

        eventBus?.publishEvent(IngredientCreatedEvent(itemId = meta.id))
    }

    /**
     * Update ingredient metadata via command.
     */
    suspend fun updateIngredientMetadata(metadata: IngredientMetadata) {
        Log.d("Remmi", "[IngredientActions] - Updating metadata for ${metadata.id}")
        val updated = metadata.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
        metadataRepo.update(updated)
        eventBus?.publishCommand(UpsertDataCommand(tableName = "ingredient_metadata", item = updated, serializer = IngredientMetadata.serializer()))
        eventBus?.publishEvent(IngredientUpdatedEvent(itemId = metadata.id))
    }

    /**
     * Adjust stock quantity via commands. 
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
            batchRepo.add(batch)
            eventBus?.publishCommand(UpsertDataCommand(tableName = "stock_batches", item = batch, serializer = StockBatch.serializer()))
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
                    batchRepo.remove(batch.id)
                    eventBus?.publishCommand(DeleteDataCommand(tableName = "stock_batches", itemId = batch.id))
                } else {
                    val updatedBatch = batch.copy(
                        quantity = batch.quantity - remainingToDeduct,
                        modified = now
                    )
                    batchRepo.update(updatedBatch)
                    eventBus?.publishCommand(UpsertDataCommand(tableName = "stock_batches", item = updatedBatch, serializer = StockBatch.serializer()))
                    remainingToDeduct = 0.0
                }
            }
            eventBus?.publishEvent(IngredientStockAdjustedEvent(itemId = stockId, delta = delta))
        }
    }

    suspend fun sync() {
        eventBus?.publishCommand(FetchAllDataCommand(tableName = "ingredient_metadata", serializer = IngredientMetadata.serializer()))
        eventBus?.publishCommand(FetchAllDataCommand(tableName = "user_stock", serializer = UserStock.serializer()))
        eventBus?.publishCommand(FetchAllDataCommand(tableName = "stock_batches", serializer = StockBatch.serializer()))
    }

    // ----------------------------------------------------------------------------
    //                               RECEIPT SCANNING
    // ----------------------------------------------------------------------------

    /**
     * Start the receipt scanning process.
     */
    suspend fun startReceiptScan(useCamera: Boolean) {
        Log.i("Remmi", "[IngredientActions] - Starting receipt scan (useCamera: $useCamera)")
        eventBus?.publishCommand(
            RequestReceiptImageCommand(useCamera = useCamera)
        )
    }

    /**
     * Process recognized text into items and match them.
     */
    suspend fun processRecognizedText(text: String): List<ReceiptItemMatch> {
        Log.i("Remmi", "[IngredientActions] - Processing recognized text")
        val parser = ReceiptParser()
        val items = parser.parse(text)
        
        val matcher = IngredientReceiptMatcher(metadataRepo.getAll())
        return matcher.match(items)
    }

    /**
     * Finalize stock updates from confirmed receipt items.
     */
    suspend fun processConfirmedReceiptItems(confirmedMatches: List<ReceiptItemMatch>) {
        Log.i("Remmi", "[IngredientActions] - Processing ${confirmedMatches.size} confirmed items")

        confirmedMatches.filter { it.status == MatchStatus.CONFIRMED && it.matchedIngredient != null }.forEach { match ->
            val ingredient = match.matchedIngredient!!
            val item = match.receiptItem
            
            // Find existing user stock for this metadata
            val userStock = stockRepo.getAll().find { it.metadataId == ingredient.id }
            
            if (userStock != null) {
                // Adjust existing stock
                adjustStock(
                    stockId = userStock.id,
                    delta = item.quantity ?: 1.0,
                    expiryDate = null
                )
            } else {
                // Create new stock association and batch
                addIngredient(
                    name = ingredient.name,
                    foodGroup = ingredient.foodGroup,
                    initialQuantity = item.quantity ?: 1.0,
                    unit = item.unit ?: MeasurementUnit.UNITS,
                    expiryDate = null,
                    brand = ingredient.brand,
                    description = ingredient.description,
                    storageLocation = StorageLocation.PANTRY,
                    allowedUnits = ingredient.allowedUnits,
                    conversions = ingredient.conversions,
                    baseNutrition = ingredient.baseNutrition,
                    shelfLife = ingredient.estimatedShelfLifeMinDays to ingredient.estimatedShelfLifeMaxDays
                )
            }
        }
    }
}
