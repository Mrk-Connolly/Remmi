package com.remmi.app.core.model.ingredients

/**
 * NUTRITION CONSTANTS
 * 
 * Standard daily reference values for an average adult (2000 kcal diet).
 * Used for normalizing the radar graph and calculating % of daily intake.
 */
object NutritionConstants {
    
    // Main nutrients daily reference values
    const val REF_CALORIES = 2000.0 // kcal
    const val REF_PROTEIN = 50.0   // grams
    const val REF_CARBS = 275.0    // grams
    const val REF_SUGAR = 50.0     // grams (Recommended max)
    const val REF_FAT = 78.0       // grams
    const val REF_FIBER = 28.0     // grams
    const val REF_SODIUM = 2300.0  // milligrams (Recommended max)
    
    // Axis names for the radar graph
    val RADAR_AXES = listOf(
        "Fiber",
        "Sugar",
        "Carbs",
        "Fats",
        "Proteins",
        "Sodium"
    )
}
