package com.remmi.app.plugins.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.ui.components.RemmiCard
import com.remmi.app.plugins.weather.ui.screens.getWeatherIcon

class WeatherWidget(
    override val metadata: PluginMetadata,
    private val actions: WeatherActions
) : RemmiWidget {

    @Composable
    override fun Content() {
        val weatherData by actions.weatherData
        val isLoading by actions.isLoading

        LaunchedEffect(Unit) {
            if (weatherData == null) {
                actions.fetchWeatherData()
            }
        }

        RemmiCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Weather",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    if (isLoading && weatherData == null) {
                        Text("Loading...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        weatherData?.let {
                            Text(
                                text = "${it.currentTemp.toInt()}°",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = it.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } ?: Text("Unavailable", style = MaterialTheme.typography.bodySmall)
                    }
                }
                weatherData?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getWeatherIcon(it.icon),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
