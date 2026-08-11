package com.remmi.app.plugins.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.widgets.RemmiWidget

/**
 * Dashboard widget for favorite contacts.
 */
class ContactWidget(private val actions: ContactActions) : RemmiWidget {

    @Composable
    override fun Content() {
        var favorites by remember { mutableStateOf(emptyList<ContactItem>()) }

        LaunchedEffect(Unit) {
            favorites = actions.getAllContacts().filter { it.isFavorite }.take(5)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⭐ Favorite Contacts",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                if (favorites.isEmpty()) {
                    Text("No favorites yet", style = MaterialTheme.typography.bodySmall)
                } else {
                    favorites.forEach { contact ->
                        Text(
                            text = "• ${contact.name} ${contact.surname}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
