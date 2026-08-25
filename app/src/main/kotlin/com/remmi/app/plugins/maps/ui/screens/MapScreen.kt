package com.remmi.app.plugins.maps.ui.screens

import android.util.Log
import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.maps.MapActions
import com.remmi.app.core.model.maps.SavedLocation
import com.remmi.app.core.model.calendar.CalendarItem
import com.remmi.app.plugins.contacts.ContactItem
import com.remmi.app.plugins.calendar.CalendarActions
import com.remmi.app.plugins.contacts.ContactActions
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
fun MapScreen(
    actions: MapActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[MapScreen] - Executed")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    var savedLocations by remember { mutableStateOf(emptyList<SavedLocation>()) }
    var otherMarkers by remember { mutableStateOf(emptyList<Feature<Point, JsonObject>>()) }

    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = Position(0.0, 0.0), zoom = 1.0)
    )

    LaunchedEffect(Unit) {
        savedLocations = actions.getAllSavedLocations()
        
        // Fetch Calendar Events and Contacts
        scope.launch {
            val calendarActions = controller.pluginManager.plugins["calendar"]?.actions as? CalendarActions
            val contactActions = controller.pluginManager.plugins["contacts"]?.actions as? ContactActions
            
            val events = calendarActions?.getAllEvents() ?: emptyList()
            val contacts = contactActions?.getAllContacts() ?: emptyList()
            
            val markers = mutableListOf<Feature<Point, JsonObject>>()
            
            // Geocode Calendar Locations
            events.forEach { event ->
                event.location.forEach { locStr ->
                    geocode(locStr, geocoder, actions)?.let { pos ->
                        markers.add(Feature(
                            geometry = Point(pos),
                            properties = buildJsonObject { 
                                put("title", "Event: ${event.title}") 
                                put("type", "calendar")
                            }
                        ))
                    }
                }
            }
            
            // Geocode Contact Addresses (if available)
            contacts.forEach { _ ->
                // No explicit address in ContactItem yet
            }
            
            otherMarkers = markers
        }
    }

    val savedFeatures = remember(savedLocations) {
        savedLocations.mapNotNull { loc ->
            if (loc.latitude != null && loc.longitude != null) {
                Feature(
                    geometry = Point(Position(loc.longitude!!, loc.latitude!!)),
                    properties = buildJsonObject { 
                        put("title", loc.name)
                        put("type", "saved")
                    }
                )
            } else null
        }
    }
    
    val allFeatures = remember(savedFeatures, otherMarkers) {
        savedFeatures + otherMarkers
    }

    val mapSource = rememberGeoJsonSource(data = GeoJsonData.Features(FeatureCollection(allFeatures)))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Map View") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    scope.launch {
                        controller.eventBus.publishCommand(
                            com.remmi.app.core.events.commands.PickLocationCommand(requestId = "self_save")
                        )
                    }
                },
                modifier = Modifier.padding(bottom = 168.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Add Current Location")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Demo
            ) {
                SymbolLayer(
                    id = "all-locations-layer",
                    source = mapSource,
                    iconImage = image("marker_15"),
                    textField = format(span(feature["title"].asString()))
                )
            }
        }
    }
}

private suspend fun geocode(query: String, geocoder: Geocoder, actions: MapActions): Position? {
    actions.getCachedLocation(query)?.let { return Position(it.second, it.first) }
    
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
