package com.remmi.app.plugins.gift

import android.util.Log
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.events.EventType
import com.remmi.app.core.events.PluginEvent
import com.remmi.app.core.plugins.actions.RemmiAction
import kotlin.time.Instant
import java.util.UUID

/**
 * Action controller for the Gift plugin.
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
     * Create and insert a new gift idea for a specific contact
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
            repository.insert(idea)

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully created gift idea: ${idea.id}. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "gift",
                    type = EventType.CREATED,
                    itemId = idea.id
                )
            )

            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to add gift idea", e)
            false
        }
    }

    /**                                 Update Gift Idea
     * Update details of an existing gift idea
     * */
    suspend fun updateGiftIdea(idea: GiftIdea): Boolean {
        Log.d("Remmi", "[GiftActions] - [updateGiftIdea] executed")
        return try {
            idea.modified = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
            repository.updateCloud(idea)

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully updated gift idea: ${idea.id}. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "gift",
                    type = EventType.UPDATED,
                    itemId = idea.id
                )
            )

            true
        } catch (e: Exception) {
            Log.e("GiftActions", "Failed to update gift idea", e)
            false
        }
    }

    /**                                 Delete Gift Idea
     * Remove a gift idea from the repository by ID
     * */
    suspend fun deleteGiftIdea(id: String): Boolean {
        Log.d("Remmi", "[GiftActions] - [deleteGiftIdea] executed")
        return try {
            repository.delete(id)

            // Publish Fact
            Log.i("Remmi", "[GiftActions] - Successfully deleted gift idea: $id. Publishing event...")
            eventBus?.publishEvent(
                PluginEvent(
                    source = "gift",
                    type = EventType.DELETED,
                    itemId = id
                )
            )

            true
        } catch (e: Exception) {
            false
        }
    }

    /**                                 Get Ideas for Contact
     * Retrieve all gift ideas associated with a specific contact
     * */
    fun getGiftIdeasForContact(contactId: String): List<GiftIdea> {
        Log.d("Remmi", "[GiftActions] - [getGiftIdeasForContact] executed")
        return repository.getAll().filter { it.contactId == contactId }.sortedByDescending { it.created }
    }

    /**                                 Sync
     * Synchronize gift ideas with the cloud
     * */
    suspend fun sync() {
        Log.d("Remmi", "[GiftActions] - [sync] executed")
        repository.sync()
    }
}
