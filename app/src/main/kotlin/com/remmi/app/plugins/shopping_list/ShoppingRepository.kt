package com.remmi.app.plugins.shopping_list

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.shopping_list.models.ShoppingItem

class ShoppingRepository : MemoryRepository<ShoppingItem>() {
    init {
        Log.d("Remmi", "[ShoppingRepository] - Initialized")
    }
}
