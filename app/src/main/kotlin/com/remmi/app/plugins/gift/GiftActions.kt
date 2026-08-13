package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.actions.RemmiAction
import kotlinx.datetime.Instant
import java.util.UUID

class GiftActions(
    private val repository: GiftRepository,
    override val id: String = "gift_actions",
    override val name: String = "Gift Actions"
) : RemmiAction {

    init {
        Log.d("Remmi", "[GiftActions] - [constructor] executed")
    }

    suspend fun addGiftIdea(
        contactId: String,
        name: String,
        description: String?,
        link: String?,
        price: Double?,
        event: GiftEvent?
    ): Boolean {
        Log.d("Remmi", "[GiftActions] - [addGiftIdea] executed")
        return try {
            val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            val idea = GiftIdea(
                id = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                contactId = contactId,
                name = name,
                description = description,
                link = link,
                price = price,
                event = event
            )
            repository.insert(idea)
            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to add gift idea", e)
            false
        }
    }

    suspend fun updateGiftIdea(idea: GiftIdea): Boolean {
        Log.d("Remmi", "[GiftActions] - [updateGiftIdea] executed")
        return try {
            idea.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(idea)
            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to update gift idea", e)
            false
        }
    }

    suspend fun deleteGiftIdea(id: String): Boolean {
        Log.d("Remmi", "[GiftActions] - [deleteGiftIdea] executed")
        return try {
            repository.delete(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getGiftIdeasForContact(contactId: String): List<GiftIdea> {
        Log.d("Remmi", "[GiftActions] - [getGiftIdeasForContact] executed")
        return repository.getAll().filter { it.contactId == contactId }.sortedByDescending { it.created }
    }

    suspend fun sync() {
        Log.d("Remmi", "[GiftActions] - [sync] executed")
        repository.sync()
    }
}
