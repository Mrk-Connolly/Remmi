package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.database.DatabaseService

class GiftRepository(databaseService: DatabaseService) : CloudRepository<GiftIdea>(
    databaseService = databaseService,
    tableName = "gift_ideas",
    serializer = GiftIdea.serializer()
) {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Gift Repository
     * */
    init {
        Log.d("Remmi", "[GiftRepository] - Constructor initialized")
    }

}
