package com.remmi.app.plugins.gift

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.ui.screens.GiftListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GiftPlugin(
    override val metadata: PluginMetadata,
    private val pluginManager: PluginManager
) : RemmiPlugin {

    init {
        Log.d("Remmi", "[GiftPlugin] - [constructor] executed")
    }

    override val repository: GiftRepository = GiftRepository(SupabaseService)
    override val actions: GiftActions = GiftActions(repository)
    
    // The Gift List plugin requires access to Contacts
    private val contactActions: ContactActions?
        get() = (pluginManager.plugins["contacts"] as? ContactPlugin)?.actions

    override val widget: RemmiWidget = object : RemmiWidget {
        @Composable override fun Content() {
            Log.d("Remmi", "[GiftPlugin] - [Content] (widget) executed")
            // Placeholder for gift widget if needed later
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() {
            Log.d("Remmi", "[GiftPlugin] - [Content] (screen) executed")
            contactActions?.let {
                GiftListScreen(giftActions = actions, contactActions = it)
            }
        }
    }

    override fun onLoad() {
        Log.d("Remmi", "[GiftPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Gift Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync gifts: ${e.message}")
            }
        }
    }

    override fun onUnload() {
        Log.d("Remmi", "[GiftPlugin] - [onUnload] executed")
    }

    override suspend fun reformat() {
        Log.d("Remmi", "[GiftPlugin] - [reformat] executed")
        repository.clearAll()
    }
}
