package br.com.watchusee.android.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

fun cardFadeInAnimation(delayMillis: Int = 0): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { it / 3 },
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        )
    )
}

@Composable
fun Modifier.scaleOnClick(
    targetScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        label = "scaleOnClick"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
}

@Composable
fun ScreenTransition(
    targetState: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "ScreenTransition"
    ) { _ ->
        content()
    }
}
@Composable
fun shimmerBrush(): Brush {
    val colors = listOf(
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.4f),
        Color.Gray.copy(alpha = 0.2f)
    )

    return Brush.horizontalGradient(
        colors = colors,
        startX = 0f,
        endX = Float.POSITIVE_INFINITY
    )
}