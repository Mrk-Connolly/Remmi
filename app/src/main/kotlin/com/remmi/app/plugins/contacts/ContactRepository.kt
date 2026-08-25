package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.plugin.repository.CloudRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * Repository for managing [ContactItem] data.
 */
class ContactRepository(
    databaseService: DatabaseService,
    authRepository: AuthRepository? = null
) : CloudRepository<ContactItem>(
    databaseService = databaseService,
    tableName = "contacts_TEST",
    serializer = ContactItem.serializer(),
    authRepository = authRepository
) {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Contact Repository
     * */
    init {
        Log.d("Remmi", "[ContactRepository] - Constructor initialized")
    }

}
