package com.remmi.app.core.android

/**
 * CONTACT SERVICE
 *
 * Interface for standard contact operations.
 */
interface ContactService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Fetch Contacts
     * Retrieve contacts from the system.
     * */
    fun fetchContacts(): List<Any>

    /**                                 Create Contact
     * Create a new contact in the system.
     * */
    fun createContact(name: String, phone: String)
}
