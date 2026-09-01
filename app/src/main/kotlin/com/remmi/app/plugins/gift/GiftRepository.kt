package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.gift.models.GiftIdea

/**
 * Repository for managing [GiftIdea] data via in-memory caching.
 */
class GiftRepository : MemoryRepository<GiftIdea>() {

    init {
        Log.d("Remmi", "[GiftRepository] - Constructor initialized")
    }
}
