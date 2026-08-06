package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
data class Relationship(

    val targetId: String,

    val type: RelationshipType

)