package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.ContactCreatedEvent
import com.remmi.app.core.eventBus.events.ContactDeletedEvent
import com.remmi.app.core.eventBus.events.ContactUpdatedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.contacts.models.ContactItem
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Action controller for the Contacts plugin via EventBus.
 */
class ContactActions(
    private val repository: ContactRepository,
    override val id: String = "contacts_actions",
    override val name: String = "Contacts Actions"
) : RemmiAction {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system event bus */
    override var eventBus: EventBus? = null


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Contact Actions
     * */
    init {
        Log.d("Remmi", "[ContactActions] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Create Contact
     * Create and insert a new contact via commands
     * */
    suspend fun createContact(
        name: String,
        surname: String,
        nickname: String?,
        phone: String?,
        email: String?,
        birthday: String?,
        group: String,
        inGiftList: Boolean = false,
        isFavorite: Boolean = false
    ): Boolean {
        Log.d("Remmi", "[ContactActions] - [createContact] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val contact = ContactItem(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                name = name,
                surname = surname,
                nickname = nickname,
                mobilePhone = phone,
                email = email,
                birthday = birthday,
                group = group,
                isFavorite = isFavorite,
                inGiftList = inGiftList
            )
            
            // 1. Update local cache
            repository.add(contact)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "contacts",
                    item = contact,
                    serializer = ContactItem.serializer()
                )
            )

            // Publish Fact
            Log.i("Remmi", "[ContactActions] - Successfully created contact: ${contact.id}. Publishing event...")
            eventBus?.publishEvent(
                ContactCreatedEvent(itemId = contact.id)
            )

            true
        } catch (e: Exception) {
            Log.e("ContactActions", "Failed to create contact", e)
            false
        }
    }

    /**                                 Update Contact
     * Update contact details via commands
     * */
    suspend fun updateContact(contact: ContactItem): Boolean {
        Log.d("Remmi", "[ContactActions] - [updateContact] executed")
        return try {
            val updatedContact = contact.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
            
            // 1. Update local cache
            repository.update(updatedContact)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "contacts",
                    item = updatedContact,
                    serializer = ContactItem.serializer()
                )
            )

            // Publish Fact
            Log.i("Remmi", "[ContactActions] - Successfully updated contact: ${contact.id}. Publishing event...")
            eventBus?.publishEvent(
                ContactUpdatedEvent(itemId = contact.id)
            )

            true
        } catch (e: Exception) {
            Log.e("ContactActions", "Failed to update contact", e)
            false
        }
    }

    /**                                 Delete Contact
     * Delete a contact by ID via commands
     * */
    suspend fun deleteContact(id: String): Boolean {
        Log.d("Remmi", "[ContactActions] - [deleteContact] executed")
        return try {
            // 1. Remove from local cache
            repository.remove(id)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                DeleteDataCommand(
                    tableName = "contacts",
                    itemId = id
                )
            )

            // Publish Fact
            Log.i("Remmi", "[ContactActions] - Successfully deleted contact: $id. Publishing event...")
            eventBus?.publishEvent(
                ContactDeletedEvent(itemId = id)
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    /**                                 Toggle Favorite
     * Toggle the favorite status of a contact
     * */
    suspend fun toggleFavorite(contact: ContactItem): Boolean {
        Log.d("Remmi", "[ContactActions] - [toggleFavorite] executed")
        val updated = contact.copy(isFavorite = !contact.isFavorite)
        return updateContact(updated)
    }

    /**                                 Toggle Gift List
     * Toggle whether a contact is included in the gift list
     * */
    suspend fun toggleGiftList(contact: ContactItem): Boolean {
        Log.d("Remmi", "[ContactActions] - [toggleGiftList] executed")
        val updated = contact.copy(inGiftList = !contact.inGiftList)
        return updateContact(updated)
    }

    /**                                 Get All
     * Retrieve all contacts sorted by name from local cache
     * */
    suspend fun getAllContacts(): List<ContactItem> {
        Log.d("Remmi", "[ContactActions] - [getAllContacts] executed")
        return repository.getAll().sortedBy { it.name }
    }

    /**                                 Sync
     * Synchronize contacts with the cloud via command
     * */
    suspend fun sync() {
        Log.d("Remmi", "[ContactActions] - [sync] executed")
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "contacts",
                serializer = ContactItem.serializer()
            )
        )
    }
}
