package com.remmi.app.plugins.tasks

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.remmi.app.core.widgets.RemmiWidget

class TasksWidget : RemmiWidget {
    @Composable
    override fun Content() {
        Card {
            Text("✅ Tasks")
        }
    }
}
