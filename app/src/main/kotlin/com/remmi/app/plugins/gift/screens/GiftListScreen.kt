package com.remmi.app.plugins.gift.screens

import com.remmi.app.plugins.gift.GiftActions
import com.remmi.app.plugins.gift.GiftIdea
import com.remmi.app.plugins.gift.popups.*

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftListScreen(
    giftActions: GiftActions,
    contactActions: ContactActions
) {
    Log.d("Remmi", "[GiftListScreen] - [GiftListScreen] executed")
    val scope = rememberCoroutineScope()
    var contactsInGiftList by remember { mutableStateOf(emptyList<ContactItem>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showContactPicker by remember { mutableStateOf(false) }
    var selectedContactForGifts by remember { mutableStateOf<ContactItem?>(null) }
    var contactToRemove by remember { mutableStateOf<ContactItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = remember {
        {
            scope.launch {
                isRefreshing = true
                contactsInGiftList = contactActions.getAllContacts().filter { it.inGiftList }
                delay(500)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        contactsInGiftList = contactActions.getAllContacts().filter { it.inGiftList }
    }

    if (selectedContactForGifts != null) {
        UserGiftListScreen(
            contact = selectedContactForGifts!!,
            actions = giftActions,
            onBack = { selectedContactForGifts = null }
        )
    } else {
        val filteredContacts = remember(contactsInGiftList, searchQuery) {
            contactsInGiftList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.surname.contains(searchQuery, ignoreCase = true)
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showContactPicker = true },
                    modifier = Modifier.padding(bottom = 240.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact")
                }
            },
            bottomBar = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 160.dp),
                    placeholder = { Text("Search gift list...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = CircleShape,
                    singleLine = true
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Gift List",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (filteredContacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No one in your gift list yet.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredContacts, key = { it.id }) { contact ->
                                GiftContactRow(
                                    contact = contact,
                                    onClick = { selectedContactForGifts = contact },
                                    onHold = { contactToRemove = contact }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showContactPicker) {
        ContactPickerList(
            contactActions = contactActions,
            onDismiss = { showContactPicker = false },
            onContactSelected = { contact ->
                scope.launch {
                    contactActions.toggleGiftList(contact)
                    contactsInGiftList = contactActions.getAllContacts().filter { it.inGiftList }
                    showContactPicker = false
                }
            }
        )
    }

    if (contactToRemove != null) {
        AlertDialog(
            onDismissRequest = { contactToRemove = null },
            title = { Text("Remove from Gift List?") },
            text = { Text("Do you want to remove ${contactToRemove!!.name} from your gift list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            contactActions.toggleGiftList(contactToRemove!!)
                            contactsInGiftList = contactActions.getAllContacts().filter { it.inGiftList }
                            contactToRemove = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToRemove = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun GiftContactRow(
    contact: ContactItem,
    onClick: () -> Unit,
    onHold: () -> Unit
) {
    Log.d("Remmi", "[GiftListScreen] - [GiftContactRow] executed")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                onHold()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(text = "${contact.name} ${contact.surname}", style = MaterialTheme.typography.titleMedium)
                Text(text = contact.group, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerList(
    contactActions: ContactActions,
    onDismiss: () -> Unit,
    onContactSelected: (ContactItem) -> Unit
) {
    Log.d("Remmi", "[GiftListScreen] - [ContactPickerList] executed")
    var allContacts by remember { mutableStateOf(emptyList<ContactItem>()) }
    
    LaunchedEffect(Unit) {
        allContacts = contactActions.getAllContacts().filter { !it.inGiftList }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add from Contacts") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (allContacts.isEmpty()) {
                    Text("All contacts are already in your gift list.")
                } else {
                    LazyColumn {
                        items(allContacts) { contact ->
                            ListItem(
                                headlineContent = { Text("${contact.name} ${contact.surname}") },
                                supportingContent = { Text(contact.group) },
                                modifier = Modifier.clickable { onContactSelected(contact) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
