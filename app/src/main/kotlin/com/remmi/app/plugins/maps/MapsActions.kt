package com.remmi.app.plugins.maps

import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction

/**
 * Actions for the Maps plugin.
 * Currently minimal as the plugin only opens the map.
 */
class MapsActions(
    override val id: String = "maps_actions",
    override val name: String = "Maps Actions"
) : RemmiAction {
    override var eventBus: EventBus? = null
}
