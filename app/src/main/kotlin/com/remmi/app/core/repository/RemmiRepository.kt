package com.remmi.app.core.repository

import com.remmi.app.core.model.models.RemmiModel

/**
 * Generic interface for data persistence and retrieval.
 *
 * Implementations can manage data in-memory, in a local database (Room/SQLite),
 * or in the cloud (Supabase).
 *
 * @param T The type of [RemmiModel] managed by this repository.
 */
interface RemmiRepository<T : RemmiModel> {

    /**
     * Adds a new item to the repository.
     */
    fun add(item: T)

    /**
     * Removes an item from the repository by its [id].
     */
    fun remove(id: String)

    /**
     * Updates an existing item in the repository.
     */
    fun update(item: T)

    /**
     * Retrieves an item by its [id].
     */
    fun get(id: String): T?

    /**
     * Retrieves all items currently managed by the repository.
     */
    fun getAll(): List<T>

    /**
     * Clears all items from the repository.
     */
    fun clear()
}
