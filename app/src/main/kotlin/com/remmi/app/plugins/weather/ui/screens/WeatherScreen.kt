package com.remmi.app.plugins.weather.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.service.android.WeatherInfo
import com.remmi.app.plugins.weather.WeatherActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    actions: WeatherActions,
    controller: RemmiController
) {
    Log.d("Remmi", "[WeatherScreen] - Executing")
    
    var weatherData by remember { mutableStateOf<WeatherInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        weatherData = actions.getWeatherData()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            weatherData?.let { data ->
                WeatherContent(data, padding)
            }
        }
    }
}

@Composable
fun WeatherContent(data: WeatherInfo, padding: PaddingValues) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.background
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
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
                Icon(
                    imageVector = getWeatherIcon(data.icon),
                    contentDescription = data.summary,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${data.currentTemp}°",
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
                        value = "${data.feelsLike}°"
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
        
        item { Spacer(modifier = Modifier.height(180.dp)) }
    }
}

@Composable
fun WeatherDetailCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
