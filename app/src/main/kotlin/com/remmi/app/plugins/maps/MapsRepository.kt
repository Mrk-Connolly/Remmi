package com.remmi.app.plugins.maps

import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository

/**
 * Placeholder repository for the Maps plugin.
 * Currently, the plugin only provides a map view and does not persist data.
 */
class MapsRepository : RemmiRepository<RemmiModel> {
    override fun add(item: RemmiModel) {}
    override fun remove(id: String) {}
    override fun update(item: RemmiModel) {}
    override fun get(id: String): RemmiModel? = null
    override fun getAll(): List<RemmiModel> = emptyList()
    override fun clear() {}
}
