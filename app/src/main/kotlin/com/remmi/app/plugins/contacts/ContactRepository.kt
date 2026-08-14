package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.plugins.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

/**
 * Repository for managing [ContactItem] data.
 */
class ContactRepository(databaseService: DatabaseService) : CloudRepository<ContactItem>(
    databaseService = databaseService,
    tableName = "contacts",
    serializer = ContactItem.serializer()
) {
    init {
        Log.d("Remmi", "[ContactRepository] - [constructor] executed")
    }
}
