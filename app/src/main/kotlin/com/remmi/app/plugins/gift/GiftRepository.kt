package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.service.database.DatabaseService

class GiftRepository(
    databaseService: DatabaseService,
    authRepository: AuthRepository? = null
) : CloudRepository<GiftIdea>(
    databaseService = databaseService,
    tableName = "gift_ideas_TEST",
    serializer = GiftIdea.serializer(),
    authRepository = authRepository
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
