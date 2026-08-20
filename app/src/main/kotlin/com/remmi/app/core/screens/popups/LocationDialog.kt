package com.remmi.app.core.screens.popups

import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * LOCATION DIALOG
 * Integrated with Google Maps for selecting locations
 */
@Composable
fun LocationDialog(
    initialLocations: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // Center on current location on start
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                }
            }
        } catch (e: SecurityException) {
            Log.e("Remmi", "Location permission not granted", e)
        }
    }

    val searchLocation = {
        scope.launch {
            if (searchQuery.isNotBlank()) {
                try {
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(searchQuery, 1)
                    }
                    if (addresses?.isNotEmpty() == true) {
                        val addr = addresses[0]
                        val latLng = LatLng(addr.latitude, addr.longitude)
                        selectedLatLng = latLng
                        locationName = addr.featureName ?: searchQuery
                        locationAddress = addr.getAddressLine(0) ?: ""
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                } catch (e: Exception) {
                    Log.e("Remmi", "Search failed", e)
                }
            }
        }
    }

    val currentLocations = remember { mutableStateListOf<String>().apply { addAll(initialLocations) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Place") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { searchLocation() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                            try {
                                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                                if (addresses?.isNotEmpty() == true) {
                                    val addr = addresses[0]
                                    locationName = addr.featureName ?: "Selected Location"
                                    locationAddress = addr.getAddressLine(0) ?: ""
                                } else {
                                    locationName = "Selected Location"
                                    locationAddress = "${latLng.latitude}, ${latLng.longitude}"
                                }
                            } catch (e: Exception) {
                                locationName = "Selected Location"
                                locationAddress = "${latLng.latitude}, ${latLng.longitude}"
                            }
                        }
                    ) {
                        selectedLatLng?.let {
                            Marker(
                                state = MarkerState(position = it),
                                title = locationName,
                                snippet = locationAddress
                            )
                        }
                    }
                }

                if (selectedLatLng != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = locationName, style = MaterialTheme.typography.titleSmall)
                            Text(text = locationAddress, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (currentLocations.isNotEmpty()) {
                    Text("Added Locations:", style = MaterialTheme.typography.labelSmall)
                    Column {
                        currentLocations.forEach { loc ->
                            Text(text = "• $loc", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { 
                    if (selectedLatLng != null) {
                        currentLocations.add("$locationName ($locationAddress)")
                        selectedLatLng = null
                    }
                }) { 
                    Text("Add") 
                }

                Button(onClick = { onConfirm(currentLocations.toList()) }) { 
                    Text("Confirm") 
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}
