package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.GiftIdeaCreatedEvent
import com.remmi.app.core.eventBus.events.GiftIdeaDeletedEvent
import com.remmi.app.core.eventBus.events.GiftIdeaUpdatedEvent
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.gift.models.GiftEvent
import com.remmi.app.plugins.gift.models.GiftIdea
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Action controller for the Gift plugin via EventBus.
 */
class GiftActions(
    private val repository: GiftRepository,
    override val id: String = "gift_actions",
    override val name: String = "Gift Actions"
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
     * Constructor for Gift Actions
     * */
    init {
        Log.d("Remmi", "[GiftActions] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Add Gift Idea
     * Create and insert a new gift idea for a specific contact via command
     * */
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
            
            // 1. Update local cache
            repository.add(idea)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "gift_ideas",
                    item = idea,
                    serializer = GiftIdea.serializer()
                )
            )

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully created gift idea: ${idea.id}. Publishing event...")
            eventBus?.publishEvent(
                GiftIdeaCreatedEvent(itemId = idea.id)
            )

            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to add gift idea", e)
            false
        }
    }

    /**                                 Update Gift Idea
     * Update details of an existing gift idea via command
     * */
    suspend fun updateGiftIdea(idea: GiftIdea): Boolean {
        Log.d("Remmi", "[GiftActions] - [updateGiftIdea] executed")
        return try {
            val updatedIdea = idea.copy(modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()))
            
            // 1. Update local cache
            repository.update(updatedIdea)
            
            // 2. Persist to cloud
            eventBus?.publishCommand(
                UpsertDataCommand(
                    tableName = "gift_ideas",
                    item = updatedIdea,
                    serializer = GiftIdea.serializer()
                )
            )

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully updated gift idea: ${idea.id}. Publishing event...")
            eventBus?.publishEvent(
                GiftIdeaUpdatedEvent(itemId = idea.id)
            )

            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to update gift idea", e)
            false
        }
    }

    /**                                 Delete Gift Idea
     * Remove a gift idea from the repository by ID via command
     * */
    suspend fun deleteGiftIdea(id: String): Boolean {
        Log.d("Remmi", "[GiftActions] - [deleteGiftIdea] executed")
        return try {
            // 1. Remove from local cache
            repository.remove(id)
            
            // 2. Persist deletion to cloud
            eventBus?.publishCommand(
                DeleteDataCommand(
                    tableName = "gift_ideas",
                    itemId = id
                )
            )

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully deleted gift idea: $id. Publishing event...")
            eventBus?.publishEvent(
                GiftIdeaDeletedEvent(itemId = id)
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    /**                                 Get Ideas for Contact
     * Retrieve all gift ideas associated with a specific contact from cache
     * */
    fun getGiftIdeasForContact(contactId: String): List<GiftIdea> {
        Log.d("Remmi", "[GiftActions] - [getGiftIdeasForContact] executed")
        return repository.getAll().filter { it.contactId == contactId }.sortedByDescending { it.created }
    }

    /**                                 Sync
     * Synchronize gift ideas with the cloud via command
     * */
    suspend fun sync() {
        Log.d("Remmi", "[GiftActions] - [sync] executed")
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "gift_ideas",
                serializer = GiftIdea.serializer()
            )
        )
    }
}
