package com.remmi.app.plugins.calendar

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.service.database.DatabaseService
import kotlinx.datetime.*

/**
 * Repository implementation for managing [CalendarItem] data.
 */
class CalendarRepository (
    databaseService: DatabaseService,
    authRepository: AuthRepository? = null
) : CloudRepository<CalendarItem>(
    databaseService = databaseService,
    tableName = "calendar_TEST",
    serializer = CalendarItem.serializer(),
    authRepository = authRepository
) {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Calendar Repository
     * Initializes with sample data for demonstration
     * */
    init {
        Log.d("Remmi", "[CalendarRepository] - Constructor initialized")
    }

}
