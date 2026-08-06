package com.remmi.app.core.model.components

import kotlinx.serialization.Serializable

@Serializable
enum class RelationshipType {

    RELATED,

    DEPENDS_ON,

    CREATED_FROM,

    REMINDS

}