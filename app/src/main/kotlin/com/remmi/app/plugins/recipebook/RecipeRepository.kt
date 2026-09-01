package com.remmi.app.plugins.recipebook

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.recipebook.models.RecipeItem

/**
 * Repository for managing [RecipeItem] data via in-memory caching.
 */
class RecipeRepository : MemoryRepository<RecipeItem>() {

    init {
        Log.d("Remmi", "[RecipeRepository] - Constructor initialized")
    }
}
