package com.remmi.app.plugins.contacts

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
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

    suspend fun updateContact(contact: ContactItem): Boolean {
        return try {
            contact.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(contact)
            true
        } catch (e: Exception) {
            Log.e("ContactActions", "Failed to update contact", e)
            false
        }
    }

    suspend fun deleteContact(id: String): Boolean {
        return try {
            repository.delete(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleFavorite(contact: ContactItem): Boolean {
        val updated = contact.copy(isFavorite = !contact.isFavorite)
        return updateContact(updated)
    }

    suspend fun toggleGiftList(contact: ContactItem): Boolean {
        val updated = contact.copy(inGiftList = !contact.inGiftList)
        return updateContact(updated)
    }

    suspend fun getAllContacts(): List<ContactItem> {
        return repository.getAll().sortedBy { it.name }
    }

    suspend fun sync() {
        repository.sync()
    }
}
