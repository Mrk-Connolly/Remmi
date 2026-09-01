package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.plugin.repository.MemoryRepository
import com.remmi.app.plugins.contacts.models.ContactItem

/**
 * Repository for managing [ContactItem] data via in-memory caching.
 */
class ContactRepository : MemoryRepository<ContactItem>() {

    init {
        Log.d("Remmi", "[ContactRepository] - Constructor initialized")
    }
}
