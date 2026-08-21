package com.remmi.app.plugins.ingredients.repository

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.database.DatabaseService
import com.remmi.app.plugins.ingredients.models.IngredientMetadata
import com.remmi.app.plugins.ingredients.models.StockBatch
import com.remmi.app.plugins.ingredients.models.UserStock

class MetadataRepository(databaseService: DatabaseService) : CloudRepository<IngredientMetadata>(
    databaseService = databaseService,
    tableName = "ingredient_metadata",
    serializer = IngredientMetadata.serializer()
) {
    init {
        Log.d("Remmi", "[MetadataRepository] - Constructor initialized")
    }
}

class StockRepository(databaseService: DatabaseService) : CloudRepository<UserStock>(
    databaseService = databaseService,
    tableName = "user_stock",
    serializer = UserStock.serializer()
) {
    init {
        Log.d("Remmi", "[StockRepository] - Constructor initialized")
    }
}

class BatchRepository(databaseService: DatabaseService) : CloudRepository<StockBatch>(
    databaseService = databaseService,
    tableName = "stock_batches",
    serializer = StockBatch.serializer()
) {
    init {
        Log.d("Remmi", "[BatchRepository] - Constructor initialized")
    }
}
