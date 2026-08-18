package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.service.database.DatabaseService

/**
 * Repository for managing [ContactItem] data.
 */
class ContactRepository(databaseService: DatabaseService) : CloudRepository<ContactItem>(
    databaseService = databaseService,
    tableName = "contacts",
    serializer = ContactItem.serializer()
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
