package com.remmi.app.plugins.calendar

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.remmi.app.core.widgets.RemmiWidget

class CalendarWidget : RemmiWidget {

    @Composable
    override fun Content() {

        Card {

            Text("📅 Calendar")
            Text("No meetings")

        }

    }

}