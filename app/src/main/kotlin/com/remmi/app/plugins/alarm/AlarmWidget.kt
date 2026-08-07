package com.remmi.app.plugins.alarm

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.remmi.app.core.widgets.RemmiWidget

class AlarmWidget : RemmiWidget {
    @Composable
    override fun Content() {
        Card {
            Text("⏰ Alarms")
        }
    }
}
