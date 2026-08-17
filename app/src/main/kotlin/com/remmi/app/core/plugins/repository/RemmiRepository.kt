package com.remmi.app.core.plugins.repository

import com.remmi.app.core.plugins.model.models.RemmiModel

/**
 * Generic interface for data persistence and retrieval.
 *
 * Implementations can manage data in-memory, in a local database (Room/SQLite),
 * or in the cloud (Supabase).
 *
 * @param T The type of [RemmiModel] managed by this repository.
 */
interface RemmiRepository<T : RemmiModel> {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Add
     * Adds a new item to the repository.
     */
    fun add(item: T)

    /**                                   Remove
     * Removes an item from the repository by its [id].
     */
    fun remove(id: String)

    /**                                   Update
     * Updates an existing item in the repository.
     */
    fun update(item: T)

    /**                                   Get
     * Retrieves an item by its [id].
     */
    fun get(id: String): T?

    /**                                   Get All
     * Retrieves all items currently managed by the repository.
     */
    fun getAll(): List<T>

    /**                                   Clear
     * Clears all items from the repository.
     */
    fun clear()
}
