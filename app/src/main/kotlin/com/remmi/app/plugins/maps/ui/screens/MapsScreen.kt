package com.remmi.app.plugins.maps.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.plugins.maps.MapsActions
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
        firstPosition = CameraPosition(target = Position(0.0, 0.0), zoom = 1.0)
    )

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
