package com.remmi.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiThemeMode

object DesignTokens {
    val CornerRadiusSmall = 12.dp
    val CornerRadiusMedium = 20.dp
    val CornerRadiusLarge = 32.dp
    
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
}

val PrimaryPalette = listOf(
    "#7F3DFF", // Deep Purple
    "#0077FF", // Ocean Blue
    "#00B159", // Sage Green
    "#FF9F00", // Sunset Orange
    "#FF4081", // Rose Pink
    "#00BFA5"  // Modern Teal
)

@Composable
fun RemmiTheme(
    content: @Composable () -> Unit
) {
    val themeMode = GlobalUIState.themePreference.value
    val darkTheme = when (themeMode) {
        RemmiThemeMode.LIGHT -> false
        RemmiThemeMode.DARK -> true
        RemmiThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val primaryColor = Color(android.graphics.Color.parseColor(GlobalUIState.primaryColorHex.value))

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = Color.White,
            background = Color.Black,
            surface = Color(0xFF121212),
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = Color.LightGray,
            outline = Color.DarkGray
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.1f),
            onPrimaryContainer = primaryColor,
            secondary = Color.Black,
            background = Color(0xFFF8F9FA),
            surface = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFF1F2F6),
            onSurfaceVariant = Color.Gray,
            outline = Color.LightGray
        )
    }

    val typography = Typography(
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
        titleMedium = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    )

    val shapes = Shapes(
        small = RoundedCornerShape(DesignTokens.CornerRadiusSmall),
        medium = RoundedCornerShape(DesignTokens.CornerRadiusMedium),
        large = RoundedCornerShape(DesignTokens.CornerRadiusLarge)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
