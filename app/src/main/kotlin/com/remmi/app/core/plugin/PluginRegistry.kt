package com.remmi.app.core.plugin

import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.ingredients.IngredientPlugin
import com.remmi.app.plugins.recipebook.RecipePlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import com.remmi.app.plugins.weather.WeatherPlugin
import com.remmi.app.plugins.maps.MapsPlugin
import com.remmi.app.plugins.callrecorder.CallRecorderPlugin
import android.content.Context

/**
 * Registry of available plugin factory functions.
 * Centralizes plugin instantiation logic.
 */
object PluginRegistry {

    fun getFactory(id: String): ((PluginMetadata, EventBus, Context) -> RemmiPlugin)? {
        return factories[id]
    }

    private val factories = mapOf<String, (PluginMetadata, EventBus, Context) -> RemmiPlugin>(
        "calendar" to { metadata, eventBus, _ -> CalendarPlugin(metadata, eventBus) },
        "tasks" to { metadata, eventBus, _ -> TasksPlugin(metadata, eventBus) },
        "alarm" to { metadata, eventBus, context -> AlarmPlugin(metadata, eventBus, context) },
        "contacts" to { metadata, eventBus, _ -> ContactPlugin(metadata, eventBus) },
        "gift" to { metadata, eventBus, _ -> GiftPlugin(metadata, eventBus) },
        "recipe_book" to { metadata, eventBus, _ -> RecipePlugin(metadata, eventBus) },
        "ingredient_stock" to { metadata, eventBus, _ -> IngredientPlugin(metadata, eventBus) },
        "weather" to { metadata, eventBus, _ -> WeatherPlugin(metadata, eventBus) },
        "maps" to { metadata, eventBus, _ -> MapsPlugin(metadata, eventBus) },
        "call_recorder" to { metadata, eventBus, _ -> CallRecorderPlugin(metadata, eventBus) }
    )
}
