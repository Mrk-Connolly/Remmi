package com.remmi.app.ui.screens.launcher.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.remmi.app.core.android.launcher.AppModel
import com.remmi.app.core.android.launcher.LauncherAppService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val launcherAppService = remember { LauncherAppService(context) }

    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        allApps = launcherAppService.getInstalledApps()
        isLoading = false
    }

    val filteredApps = remember(searchQuery, allApps) {
        launcherAppService.filterApps(allApps, searchQuery)
    }

    val frequentApps = remember(allApps) {
        launcherAppService.getFrequentlyUsedApps(allApps)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed applications...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = CircleShape,
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Frequently Used Apps Section
                    if (searchQuery.isEmpty() && frequentApps.isNotEmpty()) {
                        item {
                            Text(
                                text = "FREQUENTLY USED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(frequentApps) { app ->
                            AppListItem(
                                app = app,
                                onClick = {
                                    launcherAppService.launchApp(app.packageName)
                                    onDismissRequest()
                                }
                            )
                        }
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // All Apps List Header
                    item {
                        Text(
                            text = if (searchQuery.isEmpty()) "ALL APPLICATIONS (${filteredApps.size})" else "SEARCH RESULTS (${filteredApps.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(filteredApps, key = { it.packageName + it.activityName }) { app ->
                        AppListItem(
                            app = app,
                            onClick = {
                                launcherAppService.launchApp(app.packageName)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: AppModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBitmap = remember(app.icon) {
                app.icon?.toBitmap(64, 64)?.asImageBitmap()
            }

            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}
