package com.remmi.app.plugins.maps.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.maps.MapsActions
import kotlinx.coroutines.flow.filterIsInstance
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    actions: MapsActions,
    controller: RemmiController
) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(target = Position(2.3522, 48.8566), zoom = 12.0) // Paris default
    )

    LaunchedEffect(Unit) {
        // Request current location
        controller.eventBus.publishCommand(
            com.remmi.app.core.eventBus.commands.RequestLocationCommand()
        )
        
        // Listen for response
        controller.eventBus.events
            .filterIsInstance<com.remmi.app.core.eventBus.events.CurrentLocationRespondedEvent>()
            .collect { event ->
                cameraState.animateTo(
                    CameraPosition(target = Position(event.longitude, event.latitude), zoom = 14.0)
                )
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Map View") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Demo
            )
        }
    }
}
