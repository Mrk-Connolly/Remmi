package com.remmi.app.plugins.weather

object WeatherConditionMapper {

    fun mapCodeToSummary(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }

    fun mapCodeToIcon(code: Int, isDay: Boolean = true): String {
        return when (code) {
            0, 1 -> if (isDay) "sunny" else "clear_night"
            2 -> "cloudy" // partly cloudy
            3 -> "cloudy" // overcast
            45, 48 -> "cloudy" // fog
            51, 53, 55, 56, 57 -> "rainy" // drizzle
            61, 63, 65, 66, 67 -> "rainy"
            71, 73, 75, 77 -> "stormy" // snow as stormy for now or add snow icon
            80, 81, 82 -> "rainy"
            85, 86 -> "stormy"
            95, 96, 99 -> "stormy"
            else -> "sunny"
        }
    }
}
