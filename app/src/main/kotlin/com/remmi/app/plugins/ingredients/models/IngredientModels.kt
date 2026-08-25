package com.remmi.app.plugins.ingredients.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.daysUntil

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
    GRAMS, KILOGRAMS, MILLILITERS, LITERS, UNITS, SPOONFULS, CUPS, TEASPOONS
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
data class AdditionalNutrient(
    val name: String,
    val value: Double,
    val unit: String
)

@Serializable
data class NutritionProfile(
    val calories: Double? = null,
    val proteins: Double? = null,
    val carbohydrates: Double? = null,
    val sugars: Double? = null,
    val fats: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null, // In milligrams
    @SerialName("additional_nutrients")
    val additionalNutrients: List<AdditionalNutrient> = emptyList()
)

@Serializable
data class PreparationMethod(
    val name: String, // RAW, STEAMED, etc.
    val nutrition: NutritionProfile? = null,
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
    
    @SerialName("allowed_units")
    val allowedUnits: List<MeasurementUnit> = emptyList(),
    val conversions: List<IngredientConversion> = emptyList(),
    @SerialName("base_nutrition")
    val baseNutrition: NutritionProfile? = null,
    @SerialName("nutrition_basis")
    val nutritionBasis: NutritionBasis? = null,
    @SerialName("preparation_methods")
    val preparationMethods: List<PreparationMethod> = emptyList(),
    
    @SerialName("estimated_shelf_life_min_days")
    val estimatedShelfLifeMinDays: Int? = null,
    @SerialName("estimated_shelf_life_max_days")
    val estimatedShelfLifeMaxDays: Int? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null
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
    val minimumStock: Double? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null
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
    val expiryDate: LocalDate? = null,

    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,

    @SerialName("source_item_id")
    override val sourceItemId: String? = null
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

fun Double.toCleanString(): String = if (this % 1.0 == 0.0) this.toInt().toString() else {
    "%.2f".format(this).trimEnd('0').trimEnd('.').trimEnd(',')
}

fun formatQuantity(quantity: Double, unit: MeasurementUnit): Pair<String, String> {
    return when {
        unit == MeasurementUnit.GRAMS && quantity >= 1000 -> (quantity / 1000.0).toCleanString() to "kg"
        unit == MeasurementUnit.MILLILITERS && quantity >= 1000 -> (quantity / 1000.0).toCleanString() to "l"
        else -> quantity.toCleanString() to unit.name.lowercase()
    }
}

fun convertUnit(value: Double, from: MeasurementUnit, to: MeasurementUnit, metadata: IngredientMetadata): Double {
    if (from == to) return value
    
    // Check direct conversion
    metadata.conversions.find { it.fromUnit == from && it.toUnit == to }?.let {
        return value * it.factor
    }
    
    // Check reverse conversion
    metadata.conversions.find { it.fromUnit == to && it.toUnit == from }?.let {
        return value / it.factor
    }

    // Default weights for common units if not specified
    return when {
        from == MeasurementUnit.UNITS && to == MeasurementUnit.GRAMS -> value * 150.0 // Default 150g per unit
        from == MeasurementUnit.CUPS && to == MeasurementUnit.GRAMS -> value * 240.0
        from == MeasurementUnit.SPOONFULS && to == MeasurementUnit.GRAMS -> value * 15.0
        from == MeasurementUnit.TEASPOONS && to == MeasurementUnit.GRAMS -> value * 5.0
        
        from == MeasurementUnit.GRAMS && to == MeasurementUnit.UNITS -> value / 150.0
        from == MeasurementUnit.GRAMS && to == MeasurementUnit.CUPS -> value / 240.0
        
        from == MeasurementUnit.KILOGRAMS && to == MeasurementUnit.GRAMS -> value * 1000.0
        from == MeasurementUnit.GRAMS && to == MeasurementUnit.KILOGRAMS -> value / 1000.0
        
        from == MeasurementUnit.LITERS && to == MeasurementUnit.MILLILITERS -> value * 1000.0
        from == MeasurementUnit.MILLILITERS && to == MeasurementUnit.LITERS -> value / 1000.0
        
        else -> value
    }
}
