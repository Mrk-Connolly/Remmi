package com.remmi.app.plugins.ingredients

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.ingredients.models.*

class MetadataRepository : MemoryRepository<IngredientMetadata>() {
    init {
        Log.d("Remmi", "[MetadataRepository] - Constructor initialized")
    }
}

class StockRepository : MemoryRepository<UserStock>() {
    init {
        Log.d("Remmi", "[StockRepository] - Constructor initialized")
    }
}

class BatchRepository : MemoryRepository<StockBatch>() {
    init {
        Log.d("Remmi", "[BatchRepository] - Constructor initialized")
    }
}
