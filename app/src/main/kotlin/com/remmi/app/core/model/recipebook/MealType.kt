package com.remmi.app.core.model.recipebook

import kotlinx.serialization.Serializable

@Serializable
enum class MealType {
    BREAKFAST, LUNCH, SNACK, SUPPER, OTHER
}
