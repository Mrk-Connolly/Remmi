package com.remmi.app.plugins.contacts

import com.remmi.app.core.repository.CloudRepository
import com.remmi.app.core.service.DatabaseService

/**
 * Repository for managing [ContactItem] data.
 */
class ContactRepository(databaseService: DatabaseService) : CloudRepository<ContactItem>(
    databaseService = databaseService,
    tableName = "contacts",
    serializer = ContactItem.serializer()
)
