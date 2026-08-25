package com.remmi.app.plugins.weather

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.android.system.WeatherInfo
import com.remmi.app.plugins.weather.screens.getWeatherIcon

class WeatherWidget(
    override val metadata: PluginMetadata,
    private val actions: WeatherActions
) : RemmiWidget {

    @Composable
    override fun Content() {
        var weatherData by remember { mutableStateOf<WeatherInfo?>(null) }

        LaunchedEffect(Unit) {
            weatherData = actions.getWeatherData()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Weather",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    weatherData?.let {
                        Text(
                            text = "${it.currentTemp}° - ${it.summary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } ?: Text("Loading...", style = MaterialTheme.typography.bodySmall)
                }
                weatherData?.let {
                    Icon(
                        imageVector = getWeatherIcon(it.icon),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
