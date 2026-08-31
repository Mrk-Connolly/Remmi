package com.remmi.app.plugins.maps.ui.popups

import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.maps.MapsActions
import com.remmi.app.plugins.maps.models.SavedLocation
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerPopup(
    actions: MapsActions,
    controller: RemmiController,
    requestId: String,
    initialSearch: String? = null,
    correlationId: String? = null,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    var searchQuery by remember { mutableStateOf(initialSearch ?: "") }
    var savedLocations by remember { mutableStateOf(emptyList<SavedLocation>()) }
    
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = Position(0.0, 0.0), zoom = 1.0)
    )

    LaunchedEffect(Unit) {
        savedLocations = actions.getAllSavedLocations()
        if (searchQuery.isNotBlank()) {
            geocode(searchQuery, geocoder)?.let { pos ->
                cameraState.position = CameraPosition(target = pos, zoom = 15.0)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search location...") },
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        geocode(searchQuery, geocoder)?.let { pos ->
                                            cameraState.animateTo(CameraPosition(target = pos, zoom = 15.0))
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(tonalElevation = 0.dp) {
                    Button(
                        onClick = {
                            val pos = cameraState.position.target
                            scope.launch {
                                val name = searchQuery.ifBlank { "Picked Location" }
                                com.remmi.app.core.controller.GlobalUIState.lastConfirmedCorrelationId.value = correlationId
                                actions.notifyLocationPicked(requestId, name, null, pos.latitude, pos.longitude)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Confirm Location")
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                MaplibreMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraState = cameraState,
                    baseStyle = BaseStyle.Demo
                ) {
                    val features = savedLocations.mapNotNull { loc ->
                        if (loc.latitude != null && loc.longitude != null) {
                            Feature(
                                geometry = Point(Position(loc.longitude!!, loc.latitude!!)),
                                properties = buildJsonObject { put("title", loc.name) }
                            )
                        } else null
                    }
                    val source = rememberGeoJsonSource(data = GeoJsonData.Features(FeatureCollection(features)))
                    
                    SymbolLayer(
                        id = "saved-locations-layer",
                        source = source,
                        iconImage = image("marker_15"),
                        textField = format(span(feature["title"].asString()))
                    )
                }
            }
        }
    }
}

private suspend fun geocode(query: String, geocoder: Geocoder): Position? {
    return withContext(Dispatchers.IO) {
        try {
            val results = geocoder.getFromLocationName(query, 1)
            if (results?.isNotEmpty() == true) {
                val addr = results[0]
                Position(addr.longitude, addr.latitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
