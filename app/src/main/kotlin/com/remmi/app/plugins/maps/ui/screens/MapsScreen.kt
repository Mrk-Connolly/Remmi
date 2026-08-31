package com.remmi.app.plugins.maps.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.plugin.screens.RemmiMainScreen
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

    RemmiMainScreen(
        title = "Map View"
    ) { padding ->
        com.remmi.app.core.ui.RemmiCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                baseStyle = BaseStyle.Demo
            )
        }
    }
}
