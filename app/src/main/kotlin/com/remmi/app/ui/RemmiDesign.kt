package com.remmi.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.controller.RemmiThemeMode

import android.os.Build
import androidx.compose.ui.platform.LocalContext

object DesignTokens {
    val CornerRadiusSmall = 12.dp
    val CornerRadiusMedium = 20.dp
    val CornerRadiusLarge = 28.dp
    val CornerRadiusExtraLarge = 40.dp

    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingExtraLarge = 36.dp

    val IconSizeSmall = 18.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp

    val ButtonHeight = 52.dp
    val FABSize = 64.dp
    val IconButtonSize = 44.dp

    val GlassAlpha = 0.6f
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
fun RemmiBackground(
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme() || GlobalUIState.themePreference == RemmiThemeMode.DARK
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            primaryColor.copy(alpha = if (isDark) 0.2f else 0.04f),
            primaryColor.copy(alpha = if (isDark) 0.35f else 0.08f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Subtle decorative circles for depth
        Box(
            modifier = Modifier
                .offset(x = (-120).dp, y = 150.dp)
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.25f else 0.06f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 100.dp)
                .size(500.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.2f else 0.04f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        content()
    }
}

@Composable
fun RemmiTheme(
    content: @Composable () -> Unit
) {
    val themeMode = GlobalUIState.themePreference
    val darkTheme = when (themeMode) {
        RemmiThemeMode.LIGHT -> false
        RemmiThemeMode.DARK -> true
        RemmiThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val useDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeMode == RemmiThemeMode.SYSTEM

    val primaryColor = Color(android.graphics.Color.parseColor(GlobalUIState.primaryColorHex))

    val colorScheme = when {
        useDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        useDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.45f),
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFB0B0B0),
            background = Color.Black,
            surface = Color.Black.copy(alpha = DesignTokens.GlassAlpha),
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color.Black.copy(alpha = 0.6f),
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = primaryColor.copy(alpha = 0.5f),
            outlineVariant = Color.Black.copy(alpha = 0.2f)
        )
        else -> lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.08f),
            onPrimaryContainer = primaryColor,
            secondary = Color(0xFF4A4A4A),
            background = Color.White,
            surface = Color.White.copy(alpha = DesignTokens.GlassAlpha),
            onBackground = Color.Black,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFF5F5F5).copy(alpha = 0.5f),
            onSurfaceVariant = Color(0xFF49454F),
            outline = primaryColor.copy(alpha = 0.1f),
            outlineVariant = Color.Black.copy(alpha = 0.03f)
        )
    }

    val typography = Typography(
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.2).sp
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        labelSmall = MaterialTheme.typography.labelSmall.copy(
            lineHeight = 16.sp
        )
    )

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(DesignTokens.CornerRadiusSmall / 2),
        small = RoundedCornerShape(DesignTokens.CornerRadiusSmall),
        medium = RoundedCornerShape(DesignTokens.CornerRadiusMedium),
        large = RoundedCornerShape(DesignTokens.CornerRadiusLarge),
        extraLarge = RoundedCornerShape(DesignTokens.CornerRadiusExtraLarge)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
