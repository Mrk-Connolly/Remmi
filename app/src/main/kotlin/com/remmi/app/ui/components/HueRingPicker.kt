package com.remmi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * HUE RING PICKER
 * A Material 3 dialog containing a thick circular rainbow ring for hue selection.
 */
@Composable
fun HueRingPicker(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    // Convert hex to hue
    val initialColor = try {
        Color(android.graphics.Color.parseColor(initialColorHex))
    } catch (e: Exception) {
        Color.Magenta
    }
    
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(
        android.graphics.Color.parseColor(initialColorHex),
        hsv
    )
    
    var currentHue by remember { mutableStateOf(hsv[0]) }
    val selectedColor = remember(currentHue) {
        Color.hsv(currentHue, 1f, 1f)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Choose theme colour",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(32.dp))

                // The Rainbow Ring
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RainbowHueRing(
                        hue = currentHue,
                        onHueChange = { currentHue = it }
                    )
                    
                    // Center indicator
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = selectedColor,
                        shadowElevation = 4.dp
                    ) {}
                }

                Spacer(Modifier.height(32.dp))

                // Preview Label
                Text(
                    "Selected Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))

                // Preview Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = selectedColor
                ) {}

                Spacer(Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            val hex = String.format("#%06X", 0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(currentHue, 1f, 1f)))
                            onApply(hex) 
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun RainbowHueRing(
    hue: Float,
    onHueChange: (Float) -> Unit
) {
    val spectrum = listOf(
        Color.Red, Color(0xFFFF7F00), Color.Yellow, Color.Green, 
        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
    )
    
    val ringThickness = 30.dp
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onHueChange(calculateHue(offset, size.width.toFloat(), size.height.toFloat()))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    onHueChange(calculateHue(change.position, size.width.toFloat(), size.height.toFloat()))
                }
            }
    ) {
        val radius = (size.minDimension / 2) - ringThickness.toPx() / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw the rainbow sweep
        drawCircle(
            brush = Brush.sweepGradient(spectrum, center),
            radius = radius,
            center = center,
            style = Stroke(width = ringThickness.toPx(), cap = StrokeCap.Round)
        )

        // Draw selection indicator
        val angle = (hue / 360f) * 2 * PI.toFloat()
        val indicatorX = center.x + radius * cos(angle)
        val indicatorY = center.y + radius * sin(angle)

        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 4.dp.toPx())
        )
        
        drawCircle(
            color = Color.Black.copy(alpha = 0.2f),
            radius = 13.dp.toPx(),
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun calculateHue(position: Offset, width: Float, height: Float): Float {
    val centerX = width / 2
    val centerY = height / 2
    val dx = position.x - centerX
    val dy = position.y - centerY
    
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0) angle += 360f
    return angle
}
