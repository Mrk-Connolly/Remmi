package com.remmi.app.core.plugins

data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val author: String,

    val showInNavigation: Boolean = false,
    val showWidget: Boolean = false
)