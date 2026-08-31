package com.remmi.app.plugins.contacts.screens

import com.remmi.app.plugins.contacts.ContactItem

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugin.screens.RemmiSecondaryScreen

@Composable
fun ContactDetailScreen(
    contact: ContactItem,
    onToggleGiftList: () -> Unit,
    onDismiss: () -> Unit
) {
    Log.d("Remmi", "[ContactDetailScreen] - [ContactDetailScreen] executed")
    
    RemmiSecondaryScreen(
        title = "Contact Details",
        onBack = onDismiss,
        topBarActions = {
            IconButton(onClick = onToggleGiftList) {
                Icon(
                    imageVector = Icons.Default.Redeem, 
                    contentDescription = "Toggle Gift List",
                    tint = if (contact.inGiftList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = "${contact.name} ${contact.surname}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!contact.nickname.isNullOrEmpty()) {
                    Text(
                        text = "(\"${contact.nickname}\")",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            HorizontalDivider()

            DetailItem(Icons.Default.Phone, "Mobile", contact.mobilePhone)
            DetailItem(Icons.Default.Email, "Email", contact.email)
            DetailItem(Icons.Default.Cake, "Birthday", contact.birthday)
            DetailItem(Icons.Default.Group, "Group", contact.group)
            
            Spacer(Modifier.height(40.dp))
        }
    }
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
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(20.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = if (value.isNullOrEmpty()) "Not set" else value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
