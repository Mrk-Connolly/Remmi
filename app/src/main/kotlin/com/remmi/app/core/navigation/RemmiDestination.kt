package com.remmi.app.core.navigation

sealed class RemmiDestination(val route: String, val label: String) {
    data object Home : RemmiDestination("home", "Home")
    data object Calendar : RemmiDestination("calendar", "Calendar")
    data object Tasks : RemmiDestination("tasks", "Tasks")
    data object Settings : RemmiDestination("settings", "Settings")
}