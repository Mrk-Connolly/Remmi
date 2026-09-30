package com.remmi.app.core.models

/**
 * Represents a Category navigation context that groups specific plugins together.
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val plugins: List<String>
)

/**
 * Predefined Categories available in the application.
 */
val Categories = listOf(
    Category(
        id = "planning",
        name = "Planning",
        icon = "📅",
        plugins = listOf("calendar", "tasks", "alarm")
    ),
    Category(
        id = "travel",
        name = "Travel",
        icon = "✈️",
        plugins = listOf("weather", "maps")
    ),
    Category(
        id = "home",
        name = "Home",
        icon = "🏠",
        plugins = listOf("ingredient_stock", "shopping_list")
    ),
    Category(
        id = "people",
        name = "People",
        icon = "👥",
        plugins = listOf("contacts", "gift")
    ),
    Category(
        id = "security",
        name = "Security",
        icon = "🛡️",
        plugins = listOf("call_recorder", "voice_recorder")
    ),
    Category(
        id = "health",
        name = "Health",
        icon = "❤️",
        plugins = listOf("health")
    ),
    Category(
        id = "knowledge",
        name = "Knowledge",
        icon = "📚",
        plugins = listOf("transcriptions", "voice_transcriber", "books")
    ),
    Category(
        id = "finance_career",
        name = "Finance & Career",
        icon = "💼",
        plugins = listOf("stocks", "job_search", "cv")
    )
)
