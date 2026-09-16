package com.remmi.app.plugins.contacts

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.BaseRemmiPlugin
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.contacts.models.ContactItem
import com.remmi.app.plugins.contacts.ui.screens.ContactScreen

/**
 * Entry point for the Contacts plugin via EventBus.
 */
class ContactPlugin(
    metadata: PluginMetadata,
    eventBus: EventBus
) : BaseRemmiPlugin<ContactItem>(metadata, eventBus, ContactItem::class.java) {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: ContactRepository = ContactRepository()
    private val _actions: ContactActions = ContactActions(_repository).apply {
        this.eventBus = this@ContactPlugin.eventBus
    }

    /** Repository for managing Contacts data */
    override val repository: ContactRepository get() = _repository

    /** Action controller for contact logic. */
    override val actions: ContactActions get() = _actions

    /** Dashboard widget for contacts. */
    override val widget: RemmiWidget by lazy { ContactWidget(metadata, actions) }

    /** UI screen for contact management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[ContactPlugin] - [Content] executed")
            ContactScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[ContactPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    override suspend fun initialize() {
        super.initialize()
        Log.d("Remmi", "[ContactPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Contact plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        super.onCommand(command)
        Log.d("Remmi", "[ContactPlugin] - Received command: ${command::class.simpleName}")
    }
}
