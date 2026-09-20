package com.remmi.app.plugins.transcriptions

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiWidget

object TranscriptionsWidgets {
    class MainWidget(
        override val metadata: PluginMetadata,
        private val actions: TranscriptionsActions
    ) : RemmiWidget {
        @Composable
        override fun Content() {
            Card {
                Text(text = "Transcriptions Plugin")
            }
        }
    }
}
