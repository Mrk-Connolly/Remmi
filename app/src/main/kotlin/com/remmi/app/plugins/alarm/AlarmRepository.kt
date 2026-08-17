package com.remmi.app.plugins.alarm

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

/**
 * Repository for managing [AlarmItem] data.
 *
 * Persists alarms in the cloud and provides local caching.
 */
class AlarmRepository(databaseService: DatabaseService) : CloudRepository<AlarmItem>(
    databaseService = databaseService,
    tableName = "alarms",
    serializer = AlarmItem.serializer()
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
