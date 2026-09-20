package com.remmi.app.ui.screens.homescreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remmi.app.R
import com.remmi.app.core.controller.GlobalUIState
import com.remmi.app.core.models.Categories
import com.remmi.app.core.models.Category
import com.remmi.app.ui.DesignTokens
import com.remmi.app.ui.components.RemmiCard
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onCategoryClick: (Category) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HomeTopBar(onProfileClick, onSettingsClick)
            AssistantInputField(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularMenu(
                categories = Categories,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

@Composable
fun AssistantInputField(
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    
    RemmiCard(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        elevation = 2.dp
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { 
                Text(
                    "Ask Remmi... (e.g. 'Add task')",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            trailingIcon = {
                IconButton(onClick = { /* Future Microphone Action */ }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HomeTopBar(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(DesignTokens.IconButtonSize)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
        }

        Text(
            text = "REMMI",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(DesignTokens.IconButtonSize)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CircularMenu(
    categories: List<Category>,
    onCategoryClick: (Category) -> Unit
) {
    val radius = 150.dp
    val buttonSize = 96.dp
    val isDark = isSystemInDarkTheme() || GlobalUIState.themePreference == com.remmi.app.core.controller.RemmiThemeMode.DARK

    Box(
        modifier = Modifier.size(radius * 2 + buttonSize),
        contentAlignment = Alignment.Center
    ) {
        // CENTRAL ICON (Glassmorphic Orb)
        RemmiCard(
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            elevation = 8.dp,
            containerColor = if (isDark) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = "Remmi Center",
                    modifier = Modifier.padding(20.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (!isDark) androidx.compose.ui.graphics.ColorFilter.tint(Color.White) else null
                )
            }
        }

        // CATEGORY BUTTONS
        categories.forEachIndexed { index, category ->
            val angle = (index * (360f / categories.size) - 90f) * (Math.PI / 180f)
            val xOffset = radius * cos(angle).toFloat()
            val yOffset = radius * sin(angle).toFloat()

            val entryDelay = index * 50
            val scale = remember { Animatable(0f) }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(entryDelay.toLong())
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }

            CategoryMenuButton(
                category = category,
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(buttonSize)
                    .scale(scale.value),
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryMenuButton(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RemmiCard(
            onClick = onClick,
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            elevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = category.icon,
                    fontSize = 28.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}
