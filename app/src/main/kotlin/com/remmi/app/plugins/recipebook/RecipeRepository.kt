package com.remmi.app.plugins.recipebook

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.service.database.DatabaseService
import com.remmi.app.core.model.recipebook.RecipeItem

class RecipeRepository(databaseService: DatabaseService) : CloudRepository<RecipeItem>(
    databaseService = databaseService,
    tableName = "recipes",
    serializer = RecipeItem.serializer()
) {
    init {
        Log.d("Remmi", "[RecipeRepository] - Constructor initialized")
    }
}
