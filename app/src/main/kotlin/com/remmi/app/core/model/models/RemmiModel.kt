package com.remmi.app.core.model.models

import kotlin.time.Instant

interface RemmiModel {

    val id: String

    val created: Instant

    var modified: Instant

}