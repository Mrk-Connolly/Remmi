package com.remmi.app.plugins.ingredients.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Serializable
enum class FoodGroup {
    FRUITS, VEGETABLES, GRAINS_CEREALS, PROTEIN_FOODS, DAIRY_ALTERNATIVES, FATS_OILS, OTHER
}

@Serializable
enum class StorageLocation {
    FRIDGE, FREEZER, PANTRY, CUPBOARD, OTHER
}

@Serializable
enum class MeasurementUnit {
    GRAMS, KILOGRAMS, MILLILITERS, LITERS, UNITS, SPOONFULS, CUPS
}

@Serializable
data class IngredientConversion(
    @SerialName("from_unit")
    val fromUnit: MeasurementUnit,
    @SerialName("to_unit")
    val toUnit: MeasurementUnit,
    val factor: Double,
    @SerialName("is_approximate")
    val isApproximate: Boolean = true
)

@Serializable
data class NutritionBasis(
    val amount: Double = 0.0,
    val unit: MeasurementUnit = MeasurementUnit.GRAMS
)

@Serializable
data class NutritionData(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null
)

@Serializable
data class PreparationMethod(
    val name: String, // RAW, STEAMED, etc.
    val nutrition: NutritionData? = null,
    val benefits: List<String> = emptyList(),
    val notes: String? = null
)

/**
 * GLOBAL METADATA for an ingredient
 */
@Serializable
data class IngredientMetadata(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null, // Global metadata might have null userId

    val name: String,
    val description: String = "",
    @SerialName("food_group")
    val foodGroup: FoodGroup = FoodGroup.OTHER,
    val icon: String? = null,
    val brand: String? = null,
    
    val conversions: List<IngredientConversion> = emptyList(),
    @SerialName("base_nutrition")
    val baseNutrition: NutritionData? = null,
    @SerialName("nutrition_basis")
    val nutritionBasis: NutritionBasis? = null,
    @SerialName("preparation_methods")
    val preparationMethods: List<PreparationMethod> = emptyList()
) : RemmiModel

/**
 * USER-SPECIFIC STOCK ASSOCIATION
 */
@Serializable
data class UserStock(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("metadata_id")
    val metadataId: String,
    
    @SerialName("storage_location")
    val storageLocation: StorageLocation = StorageLocation.PANTRY,
    @SerialName("primary_unit")
    val primaryUnit: MeasurementUnit = MeasurementUnit.UNITS,
    @SerialName("minimum_stock")
    val minimumStock: Double? = null
) : RemmiModel

/**
 * PHYSICAL STOCK BATCH
 */
@Serializable
data class StockBatch(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    @SerialName("user_id")
    override val userId: String? = null,

    @SerialName("stock_id")
    val stockId: String,
    
    val quantity: Double,
    @SerialName("purchase_date")
    val purchaseDate: LocalDate,
    @SerialName("expiry_date")
    val expiryDate: LocalDate? = null
) : RemmiModel

/**
 * COMBINED UI MODEL
 */
data class IngredientUiModel(
    val metadata: IngredientMetadata,
    val stock: UserStock,
    val batches: List<StockBatch>
) {
    val totalQuantity: Double get() = batches.sumOf { it.quantity }
    
    val nearestExpiry: LocalDate? get() = batches
        .filter { it.quantity > 0 }
        .mapNotNull { it.expiryDate }
        .minByOrNull { it }
}
