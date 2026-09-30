package com.remmi.app.plugins.shopping_list.models

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    override val id: String,
    override val created: Instant,
    override var modified: Instant,
    val name: String,
    val quantity: Int = 1,
    val completed: Boolean = false,
    @SerialName("user_id")
    override val userId: String? = null,
    @SerialName("source_plugin")
    override val sourcePlugin: String? = null,
    @SerialName("source_item_id")
    override val sourceItemId: String? = null
) : RemmiModel
