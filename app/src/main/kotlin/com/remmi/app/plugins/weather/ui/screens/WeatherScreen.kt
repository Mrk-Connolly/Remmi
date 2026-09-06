package com.remmi.app.plugins.weather.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.ui.components.RemmiHomeScreen
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.weather.WeatherActions
import com.remmi.app.plugins.weather.WeatherContext
import com.remmi.app.plugins.weather.models.LocationMode
import com.remmi.app.plugins.weather.models.TemperatureUnit
import com.remmi.app.plugins.weather.models.WindSpeedUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    actions: WeatherActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[WeatherScreen] - Executing")
    val context = LocalContext.current
    WeatherContext.context = context

    val weatherData by actions.weatherData
    val isLoading by actions.isLoading
    val settings by actions.settings.collectAsState()
    val searchResults by actions.searchResults

    var showSettings by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.background
        )
    )

    RemmiHomeScreen(
        title = "Weather",
        backgroundBrush = backgroundBrush,
        topBarActions = {
            IconButton(onClick = { showSettings = !showSettings }) {
                Icon(if (showSettings) Icons.Default.Close else Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    ) { padding ->
        if (showSettings) {
            WeatherSettingsView(
                settings = settings,
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchChange = { 
                    searchQuery = it
                    scope.launch { actions.searchLocations(it) }
                },
                onLocationSelected = { 
                    scope.launch { 
                        actions.selectLocation(it)
                        showSettings = false
                    }
                },
                onSettingsChange = { settings ->
                    scope.launch { actions.updateSettings(settings) }
                },
                padding = padding
            )
        } else if (isLoading && weatherData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            weatherData?.let { data ->
                WeatherContent(data, padding, settings.manualCityName)
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Weather information unavailable.")
            }
        }
    }
}

@Composable
fun WeatherContent(data: WeatherInfo, padding: PaddingValues, cityName: String?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Current Weather Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cityName ?: "Current Location",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Icon(
                    imageVector = getWeatherIcon(data.icon),
                    contentDescription = data.summary,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${data.currentTemp.toInt()}°",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data.summary,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "H:${data.temperatureMax}°  L:${data.temperatureMin.toInt()}°",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // 2. Hourly Forecast
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hourly Forecast",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(data.hourlyForecast) { hourly ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = hourly.time, style = MaterialTheme.typography.bodySmall)
                                Icon(
                                    imageVector = getWeatherIcon(hourly.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp).padding(vertical = 4.dp)
                                )
                                Text(text = "${hourly.temp.toInt()}°", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // 3. Details Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        label = "Humidity",
                        value = "${data.humidity}%"
                    )
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Air,
                        label = "Wind",
                        value = "${data.windSpeed} km/h"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WbSunny,
                        label = "UV Index",
                        value = "${data.uvIndex}"
                    )
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Visibility,
                        label = "Visibility",
                        value = "${data.visibility} km"
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Thermostat,
                        label = "Feels Like",
                        value = "${data.feelsLike.toInt()}°"
                    )
                    WeatherDetailCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "Pressure",
                        value = "${data.pressure} hPa"
                    )
                }
            }
        }

        // 4. Daily Forecast
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "7-Day Forecast",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    data.dailyForecast.forEach { daily ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = daily.day, modifier = Modifier.width(60.dp), fontWeight = FontWeight.Medium)
                            Icon(
                                imageVector = getWeatherIcon(daily.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${daily.minTemp.toInt()}° / ${daily.maxTemp.toInt()}°",
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(80.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Sun/Moon
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Sunrise", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.LightMode, null, tint = Color.Yellow)
                        Text(text = data.sunrise, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Sunset", style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.Default.DarkMode, null, tint = Color.Gray)
                        Text(text = data.sunset, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "Weather data provided by Open-Meteo.com",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun WeatherSettingsView(
    settings: com.remmi.app.plugins.weather.models.WeatherSettings,
    searchQuery: String,
    searchResults: List<com.remmi.app.plugins.weather.models.GeocodingResult>,
    onSearchChange: (String) -> Unit,
    onLocationSelected: (com.remmi.app.plugins.weather.models.GeocodingResult) -> Unit,
    onSettingsChange: (com.remmi.app.plugins.weather.models.WeatherSettings) -> Unit,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Weather Settings", style = MaterialTheme.typography.headlineSmall)

        // Location Mode
        Column {
            Text("Location", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.locationMode == LocationMode.DEVICE,
                    onClick = { onSettingsChange(settings.copy(locationMode = LocationMode.DEVICE)) }
                )
                Text("Use device location")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = settings.locationMode == LocationMode.MANUAL,
                    onClick = { onSettingsChange(settings.copy(locationMode = LocationMode.MANUAL)) }
                )
                Text("Use manual location")
            }
        }

        if (settings.locationMode == LocationMode.MANUAL) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Search City") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { if (searchQuery.isNotEmpty()) Icon(Icons.Default.Search, null) }
                )
                
                searchResults.forEach { result ->
                    ListItem(
                        headlineContent = { Text(result.name) },
                        supportingContent = { Text("${result.admin1 ?: ""}, ${result.country ?: ""}") },
                        modifier = Modifier.clickable { onLocationSelected(result) }
                    )
                }
                
                if (settings.manualCityName != null) {
                    Text(
                        "Selected: ${settings.manualCityName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Units
        Column {
            Text("Units", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Temperature")
                FilterChip(
                    selected = settings.tempUnit == TemperatureUnit.CELSIUS,
                    onClick = { onSettingsChange(settings.copy(tempUnit = TemperatureUnit.CELSIUS)) },
                    label = { Text("°C") }
                )
                FilterChip(
                    selected = settings.tempUnit == TemperatureUnit.FAHRENHEIT,
                    onClick = { onSettingsChange(settings.copy(tempUnit = TemperatureUnit.FAHRENHEIT)) },
                    label = { Text("°F") }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Wind Speed")
                FilterChip(
                    selected = settings.windUnit == WindSpeedUnit.KMH,
                    onClick = { onSettingsChange(settings.copy(windUnit = WindSpeedUnit.KMH)) },
                    label = { Text("km/h") }
                )
                FilterChip(
                    selected = settings.windUnit == WindSpeedUnit.MPH,
                    onClick = { onSettingsChange(settings.copy(windUnit = WindSpeedUnit.MPH)) },
                    label = { Text("mph") }
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Text(
            text = "Weather data provided by Open-Meteo.com",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherDetailCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, style = MaterialTheme.typography.labelSmall)
            }
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

fun getWeatherIcon(icon: String): ImageVector {
    return when (icon) {
        "sunny" -> Icons.Default.WbSunny
        "cloudy" -> Icons.Default.Cloud
        "rainy" -> Icons.Default.WaterDrop
        "stormy" -> Icons.Default.Thunderstorm
        "clear_night" -> Icons.Default.NightsStay
        else -> Icons.Default.WbCloudy
    }
}
