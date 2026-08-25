package com.remmi.app.core.Users

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * Repository for managing [User] data.
 *
 * Persists users in the cloud and provides local caching.
 */
class UserRepository(databaseService: DatabaseService) : CloudRepository<User>(
    databaseService = databaseService,
    tableName = "users_TEST",
    serializer = User.serializer()
) {

    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for User Repository
     * */
    init {
        Log.d("Remmi", "[UserRepository] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Get By ID
     * Retrieve a user profile from the cloud by its identifier.
     */
    suspend fun getById(id: String): User? {
        Log.d("Remmi", "[UserRepository] - [getById] executed")
        return databaseService.getById(tableName, id, serializer)
    }

}