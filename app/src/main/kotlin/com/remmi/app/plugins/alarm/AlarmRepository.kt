package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * Repository for managing [AlarmItem] data.
 *
 * Persists alarms in the cloud and provides local caching.
 */
class AlarmRepository(
    databaseService: DatabaseService,
    authRepository: AuthRepository? = null
) : CloudRepository<AlarmItem>(
    databaseService = databaseService,
    tableName = "alarms_TEST",
    serializer = AlarmItem.serializer(),
    authRepository = authRepository
) {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Alarm Repository
     * */
    init {
        Log.d("Remmi", "[AlarmRepository] - Constructor initialized")
    }

}
