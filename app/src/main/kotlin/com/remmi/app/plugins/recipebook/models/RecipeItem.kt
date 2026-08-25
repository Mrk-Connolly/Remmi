package com.remmi.app.plugins.recipebook.models

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant as KInstant

@Serializable
data class RecipeItem(
    override val id: String,
    override val created: KInstant,
    override var modified: KInstant,
    @SerialName("user_id")
    override var userId: String? = null,

    val title: String,
    val description: String,
    @SerialName("image_path")
    val imagePath: String? = null,
    
    val servings: Int = 1,
    
    @SerialName("prep_time")
    val prepTime: Int = 0,
    @SerialName("cooking_time")
    val cookingTime: Int = 0,
    @SerialName("oven_time")
    val ovenTime: Int = 0,
    @SerialName("resting_time")
    val restingTime: Int = 0,

    @SerialName("total_ingredient_ids")
    val totalIngredientIds: List<String> = emptyList(),
    
    val steps: List<RecipeStep> = emptyList(),

    // Keeping these for legacy or additional info
    val ingredients: List<Ingredient> = emptyList(),
    @SerialName("serving_size")
    val servingSize: String = "", 
    @SerialName("nutrition_per_serving")
    val nutritionPerServing: NutritionInfo = NutritionInfo(),
    val instructions: List<String> = emptyList(),
    @SerialName("meal_type")
    val mealType: MealType = MealType.OTHER
) : RemmiModel {
    init {
        Log.d("Remmi", "[RecipeItem] - [constructor] executed")
    }
}
