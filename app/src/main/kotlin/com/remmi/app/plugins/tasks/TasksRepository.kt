package com.remmi.app.plugins.tasks

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * Repository for managing [TaskItem] data.
 *
 * Persists tasks in the "tasks" table of the cloud database and provides
 * in-memory caching.
 */
class TasksRepository(
    databaseService: DatabaseService,
    authRepository: AuthRepository? = null
) : CloudRepository<TaskItem>(
    databaseService = databaseService,
    tableName = "tasks_TEST",
    serializer = TaskItem.serializer(),
    authRepository = authRepository
) {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Tasks Repository
     * */
    init {
        Log.d("Remmi", "[TasksRepository] - Constructor initialized")
    }

}
