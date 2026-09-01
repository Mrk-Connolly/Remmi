package com.remmi.app.ui.popups

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.plugins.ingredients.models.NutritionConstants
import com.remmi.app.plugins.ingredients.models.NutritionProfile
import com.remmi.app.plugins.recipebook.models.NutritionInfo
import kotlin.math.cos
import kotlin.math.sin

/**
 * NUTRITION RADAR GRAPH
 * 
 * A 6-axis radar chart visualizing the nutritional balance.
 * Normalizes values based on NutritionConstants.
 */
@Composable
fun NutritionRadarGraph(
    nutrition: NutritionProfile,
    modifier: Modifier = Modifier.size(220.dp)
) {
    val normalizedValues = listOf(
        (nutrition.fiber ?: 0.0) / NutritionConstants.REF_FIBER,
        (nutrition.sugars ?: 0.0) / NutritionConstants.REF_SUGAR,
        (nutrition.carbohydrates ?: 0.0) / NutritionConstants.REF_CARBS,
        (nutrition.fats ?: 0.0) / NutritionConstants.REF_FAT,
        (nutrition.proteins ?: 0.0) / NutritionConstants.REF_PROTEIN,
        (nutrition.sodium ?: 0.0) / NutritionConstants.REF_SODIUM
    ).map { it.coerceIn(0.0, 1.2) }

    RadarChart(
        values = normalizedValues,
        labels = NutritionConstants.RADAR_AXES,
        modifier = modifier
    )
}

@Composable
fun RecipeNutritionRadarGraph(
    nutrition: NutritionInfo,
    modifier: Modifier = Modifier.size(220.dp)
) {
    val normalizedValues = listOf(
        (nutrition.fiber ?: 0.0) / NutritionConstants.REF_FIBER,
        (nutrition.sugars ?: 0.0) / NutritionConstants.REF_SUGAR,
        (nutrition.carbohydrates ?: 0.0) / NutritionConstants.REF_CARBS,
        (nutrition.fats ?: 0.0) / NutritionConstants.REF_FAT,
        (nutrition.proteins ?: 0.0) / NutritionConstants.REF_PROTEIN,
        (nutrition.sodium ?: 0.0) / NutritionConstants.REF_SODIUM
    ).map { it.coerceIn(0.0, 1.2) }

    RadarChart(
        values = normalizedValues,
        labels = NutritionConstants.RADAR_AXES,
        modifier = modifier
    )
}

@Composable
private fun RadarChart(
    values: List<Double>,
    labels: List<String>,
    modifier: Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width.coerceAtMost(size.height) / 2 * 0.65f
            val numAxes = 6
            val angleStep = (2 * Math.PI / numAxes).toFloat()

            // 1. Draw Grid (3 levels)
            for (level in 1..3) {
                val levelRadius = radius * (level / 3f)
                val gridPath = Path()
                for (i in 0 until numAxes) {
                    val angle = i * angleStep - Math.PI.toFloat() / 2
                    val x = center.x + levelRadius * cos(angle)
                    val y = center.y + levelRadius * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(gridPath, gridColor, style = Stroke(width = 1.dp.toPx()))
            }

            // 2. Draw Axes
            for (i in 0 until numAxes) {
                val angle = i * angleStep - Math.PI.toFloat() / 2
                val x = center.x + radius * cos(angle)
                val y = center.y + radius * sin(angle)
                drawLine(gridColor, center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // 3. Draw Data Polygon
            if (values.size == numAxes) {
                val dataPath = Path()
                for (i in 0 until numAxes) {
                    val angle = i * angleStep - Math.PI.toFloat() / 2
                    val valueRadius = radius * values[i].toFloat()
                    val x = center.x + valueRadius * cos(angle)
                    val y = center.y + valueRadius * sin(angle)
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()
                drawPath(dataPath, primaryColor.copy(alpha = 0.3f))
                drawPath(dataPath, primaryColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
        
        // 4. Draw Labels around the box
        Box(modifier = Modifier.fillMaxSize()) {
            labels.forEachIndexed { i, label ->
                val angle = i * (2 * Math.PI / 6).toFloat() - Math.PI.toFloat() / 2
                val xPercent = 0.5f + 0.45f * cos(angle)
                val yPercent = 0.5f + 0.45f * sin(angle)
                
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.25f)
                        .align(Alignment.Center)
                        .offset(
                            x = (xPercent - 0.5f).dp * 250, // Arbitrary multiplier for alignment
                            y = (yPercent - 0.5f).dp * 250
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
