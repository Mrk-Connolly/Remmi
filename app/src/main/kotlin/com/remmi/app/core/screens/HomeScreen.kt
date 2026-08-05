package com.remmi.app.core.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.remmi.app.core.widgets.WidgetManager

@Composable
fun HomeScreen(widgetManager: WidgetManager) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hello, I'm Remmi")
        Text("Your personal assistant")
        widgetManager.getWidgets().forEach { it.Content() }
    }
}