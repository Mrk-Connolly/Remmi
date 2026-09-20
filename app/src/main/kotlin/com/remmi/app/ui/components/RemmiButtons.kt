package com.remmi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.remmi.app.ui.DesignTokens

/**
 * Standardized Primary Button for Remmi
 */
@Composable
fun RemmiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DesignTokens.ButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(DesignTokens.IconSizeMedium)
            )
            Spacer(Modifier.width(DesignTokens.SpacingSmall))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Standardized Secondary/Cancel Button for Remmi
 */
@Composable
fun RemmiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(DesignTokens.ButtonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Standardized Floating Action Button for Remmi
 */
@Composable
fun RemmiFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentDescription: String? = null
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(DesignTokens.FABSize),
        shape = CircleShape,
        containerColor = containerColor.copy(alpha = 0.9f),
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(DesignTokens.IconSizeMedium)
        )
    }
}

/**
 * Standardized Delete Button for Remmi
 */
@Composable
fun RemmiDeleteButton(
    text: String = "Delete",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(DesignTokens.ButtonHeight),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
