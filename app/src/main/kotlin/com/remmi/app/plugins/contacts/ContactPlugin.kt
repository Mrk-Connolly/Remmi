package com.remmi.app.plugins.contacts

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.widgets.RemmiWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Contacts plugin.
 */
class ContactPlugin(override val metadata: PluginMetadata) : RemmiPlugin {

    override val repository: ContactRepository = ContactRepository(SupabaseService)
    override val actions: ContactActions = ContactActions(repository)
    override val widget: RemmiWidget = ContactWidget(actions)
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() = ContactScreen(actions)
    }

    override fun onLoad() {
        Log.d("Remmi", "Loading Contacts Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync contacts: ${e.message}")
            }
        }
    }

    override fun onUnload() {}
}
