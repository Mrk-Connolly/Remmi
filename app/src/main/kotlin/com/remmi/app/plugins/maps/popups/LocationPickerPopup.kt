package com.remmi.app.plugins.maps.popups

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.maps.MapActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.compose.camera.*
import org.maplibre.compose.map.*
import org.maplibre.compose.sources.*
import org.maplibre.compose.layers.*
import org.maplibre.compose.expressions.dsl.*
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.serialization.json.JsonObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerPopup(
    actions: MapActions,
    controller: RemmiController,
    requestId: String,
    initialSearch: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    
    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf(initialSearch ?: "") }
    var isSearching by remember { mutableStateOf(false) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = Position(0.0, 0.0), zoom = 1.0)
    )

    val searchLocation = {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            keyboardController?.hide()
            scope.launch {
                try {
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocationName(searchQuery, 1)
                    }
                    if (addresses?.isNotEmpty() == true) {
                        val addr = addresses[0]
                        val pos = Position(addr.longitude, addr.latitude)
                        selectedPosition = pos
                        locationName = addr.featureName ?: searchQuery
                        locationAddress = addr.getAddressLine(0) ?: ""
                        cameraState.animateTo(CameraPosition(target = pos, zoom = 15.0))
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Remmi", "Search failed", e)
                } finally {
                    isSearching = false
                }
            }
        }
    }

    LaunchedEffect(initialSearch) {
        if (initialSearch != null) {
            searchLocation()
        }
    }

    val geoJsonData = remember(selectedPosition) {
        val features = selectedPosition?.let {
            listOf(Feature(geometry = Point(it), properties = JsonObject(emptyMap())))
        } ?: emptyList<Feature<Point, JsonObject>>()
        GeoJsonData.Features(FeatureCollection(features))
    }
    val markerSource = rememberGeoJsonSource(data = geoJsonData)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Place") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { searchLocation() }, enabled = !isSearching) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { searchLocation() })
                )

                if (isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    MaplibreMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraState = cameraState,
                        baseStyle = BaseStyle.Demo,
                        onMapClick = { pos, _ ->
                            selectedPosition = pos
                            scope.launch {
                                try {
                                    val addresses = withContext(Dispatchers.IO) {
                                        geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
                                    }
                                    if (addresses?.isNotEmpty() == true) {
                                        val addr = addresses[0]
                                        locationName = addr.featureName ?: "Selected Location"
                                        locationAddress = addr.getAddressLine(0) ?: ""
                                    } else {
                                        locationName = "Selected Location"
                                        locationAddress = "${pos.latitude}, ${pos.longitude}"
                                    }
                                } catch (e: Exception) {
                                    locationName = "Selected Location"
                                    locationAddress = "${pos.latitude}, ${pos.longitude}"
                                }
                            }
                            org.maplibre.compose.util.ClickResult.Consume
                        }
                    ) {
                        SymbolLayer(
                            id = "selected-location-marker",
                            source = markerSource,
                            iconImage = image("marker_15")
                        )
                    }
                }

                if (selectedPosition != null) {
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        actions.notifyLocationPicked(
                            requestId = requestId,
                            name = locationName,
                            address = locationAddress,
                            lat = selectedPosition?.latitude,
                            lon = selectedPosition?.longitude
                        )
                        onDismiss()
                    }
                },
                enabled = selectedPosition != null
            ) {
                Text("Confirm Selection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
