package com.remmi.app.plugins.contacts.screens

import com.remmi.app.plugins.contacts.ContactItem

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: ContactItem,
    onToggleGiftList: () -> Unit,
    onDismiss: () -> Unit
) {
    Log.d("Remmi", "[ContactDetailScreen] - [ContactDetailScreen] executed")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleGiftList) {
                    Text(if (contact.inGiftList) "Remove from Gift List" else "Add to Gift List")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        title = {
            Column {
                Text(
                    text = "${contact.name} ${contact.surname}",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (!contact.nickname.isNullOrEmpty()) {
                    Text(
                        text = "(\"${contact.nickname}\")",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem(Icons.Default.Phone, "Mobile", contact.mobilePhone)
                DetailItem(Icons.Default.Email, "Email", contact.email)
                DetailItem(Icons.Default.Cake, "Birthday", contact.birthday)
                DetailItem(Icons.Default.Group, "Group", contact.group)
            }
        }
    )
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String?) {
    Log.d("Remmi", "[ContactDetailScreen] - [DetailItem] executed")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = if (value.isNullOrEmpty()) "Not set" else value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
