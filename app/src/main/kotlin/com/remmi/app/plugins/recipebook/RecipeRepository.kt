package com.remmi.app.plugins.recipebook

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.database.DatabaseService
import com.remmi.app.plugins.recipebook.models.RecipeItem

class RecipeRepository(databaseService: DatabaseService) : CloudRepository<RecipeItem>(
    databaseService = databaseService,
    tableName = "recipes",
    serializer = RecipeItem.serializer()
) {
    init {
        Log.d("Remmi", "[RecipeRepository] - Constructor initialized")
    }
}
