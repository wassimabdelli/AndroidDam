package tn.esprit.dam.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun AnimatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    defaultElevation: Dp = 2.dp,
    pressedElevation: Dp = 8.dp,
    defaultScale: Float = 1f,
    pressedScale: Float = 0.97f,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate elevation
    val elevation by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedElevation.value else defaultElevation.value,
        animationSpec = tween(durationMillis = 150),
        label = "elevation"
    )
    
    // Animate scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else defaultScale,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(modifier = Modifier) {
            content()
        }
    }
}

