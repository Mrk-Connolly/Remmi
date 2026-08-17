package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.plugins.actions.RemmiAction
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Action controller for the Contacts plugin.
 */
class ContactActions(
    private val repository: ContactRepository,
    override val id: String = "contacts_actions",
    override val name: String = "Contacts Actions"
) : RemmiAction {


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
     * Create and insert a new contact
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
            repository.insert(contact)
            true
        } catch (e: Exception) {
            Log.e("ContactActions", "Failed to create contact", e)
            false
        }
    }

    /**                                 Update Contact
     * Update contact details in the repository
     * */
    suspend fun updateContact(contact: ContactItem): Boolean {
        Log.d("Remmi", "[ContactActions] - [updateContact] executed")
        return try {
            contact.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(contact)
            true
        } catch (e: Exception) {
            Log.e("ContactActions", "Failed to update contact", e)
            false
        }
    }

    /**                                 Delete Contact
     * Delete a contact by ID
     * */
    suspend fun deleteContact(id: String): Boolean {
        Log.d("Remmi", "[ContactActions] - [deleteContact] executed")
        return try {
            repository.delete(id)
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
     * Retrieve all contacts sorted by name
     * */
    suspend fun getAllContacts(): List<ContactItem> {
        Log.d("Remmi", "[ContactActions] - [getAllContacts] executed")
        return repository.getAll().sortedBy { it.name }
    }

    /**                                 Sync
     * Synchronize contacts with the cloud
     * */
    suspend fun sync() {
        Log.d("Remmi", "[ContactActions] - [sync] executed")
        repository.sync()
    }
}
