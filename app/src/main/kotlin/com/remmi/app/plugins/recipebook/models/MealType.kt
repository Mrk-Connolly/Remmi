package com.remmi.app.plugins.recipebook.models

import kotlinx.serialization.Serializable

@Serializable
enum class MealType {
    BREAKFAST, LUNCH, SNACK, SUPPER, OTHER
}
