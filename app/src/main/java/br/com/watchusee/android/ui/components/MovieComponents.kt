package br.com.watchusee.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.animations.cardFadeInAnimation
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.util.TmdbImageUrl
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun MovieReelsActions(
    modifier: Modifier = Modifier,
    onDetailsClick: (() -> Unit)? = null,
    onToWatchClick: (() -> Unit)? = null,
    onWatchedClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    isToWatch: Boolean = false,
    isWatched: Boolean = false,
    isLarge: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val iconSize = if (isLarge) 32.dp else 26.dp
    val spacing = if (isLarge) 20.dp else 12.dp

    Column(
        modifier = modifier.fillMaxHeight().padding(end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        onDeleteClick?.let {
            ReelsIconButton(
                icon = Icons.Outlined.Delete,
                contentDescription = "Remover",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                },
                iconSize = iconSize,
                tint = Color.White
            )
        }

        onDetailsClick?.let {
            ReelsIconButton(
                icon = Icons.Outlined.Info,
                contentDescription = "Detalhes",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                },
                iconSize = iconSize,
                tint = Color.White
            )
        }

        onWatchedClick?.let {
            ReelsIconButton(
                icon = if (isWatched) Icons.Default.Visibility else Icons.Outlined.Visibility,
                contentDescription = "Visto",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                },
                iconSize = iconSize,
                tint = if (isWatched) MaterialTheme.colorScheme.secondary else Color.White
            )
        }

        onToWatchClick?.let {
            ReelsIconButton(
                icon = if (isToWatch) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Lista",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    it()
                },
                iconSize = iconSize,
                tint = if (isToWatch) MaterialTheme.colorScheme.primary else Color.White
            )
        }
    }
}

@Composable
private fun ReelsIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(iconSize + 16.dp)
            .scaleOnClick(targetScale = 0.85f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun MoviePosterCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    showShimmer: Boolean = false,
    isToWatch: Boolean = false,
    isWatched: Boolean = false,
    onToWatchClick: (() -> Unit)? = null,
    onWatchedClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    swipeLeftIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    swipeRightIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    swipeLeftColor: Color? = null,
    swipeRightColor: Color? = null
) {
    val haptic = LocalHapticFeedback.current
    val animatedVisibility = remember { MutableTransitionState(false) }

    // Feedback Animation State
    var feedbackIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var feedbackColor by remember { mutableStateOf(Color.White) }
    val feedbackAlpha = remember { Animatable(0f) }
    val feedbackScale = remember { Animatable(0.5f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val scope = rememberCoroutineScope()

    var prevToWatch by remember { mutableStateOf(isToWatch) }
    var prevWatched by remember { mutableStateOf(isWatched) }

    LaunchedEffect(Unit) {
        animatedVisibility.targetState = true
    }

    LaunchedEffect(isToWatch, isWatched) {
        if (animatedVisibility.currentState) {
            val isRemoved = (prevToWatch && !isToWatch) || (prevWatched && !isWatched)
            val icon = when {
                isWatched && !prevWatched -> Icons.Default.Visibility
                isToWatch && !prevToWatch -> Icons.Default.Bookmark
                isRemoved -> Icons.Default.Delete
                else -> null
            }

            if (icon != null) {
                feedbackIcon = icon
                feedbackColor = when {
                    isWatched -> Color(0xFF2E7D32)
                    isToWatch -> primaryColor
                    else -> errorColor
                }
                
                scope.launch {
                    feedbackAlpha.snapTo(0.8f)
                    feedbackScale.snapTo(0.6f)
                    launch { feedbackAlpha.animateTo(0f, tween(1000)) }
                    launch { feedbackScale.animateTo(1.8f, tween(1000)) }
                }
            }
        }
        prevToWatch = isToWatch
        prevWatched = isWatched
    }

    if (showShimmer) {
        ShimmerPosterCard()
    } else {
        AnimatedVisibility(
            visibleState = animatedVisibility,
            enter = cardFadeInAnimation(delayMillis = index * 50)
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        when (value) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                onSwipeRight?.invoke()
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                onSwipeLeft?.invoke()
                                false
                            }
                            else -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = onSwipeRight != null,
                    enableDismissFromEndToStart = onSwipeLeft != null,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> swipeRightColor ?: Color.Transparent
                            SwipeToDismissBoxValue.EndToStart -> swipeLeftColor ?: Color.Transparent
                            else -> Color.Transparent
                        }
                        
                        if (color != Color.Transparent) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color.copy(alpha = 0.15f))
                                    .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                                    Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                                    swipeRightIcon ?: Icons.Default.Check else swipeLeftIcon ?: Icons.Default.Delete
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(16.dp))
                            .scaleOnClick(
                                targetScale = 0.95f,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onClick()
                                }
                            )
                    ) {
                        AsyncImage(
                            model = TmdbImageUrl.getPosterUrl(movie.posterPath),
                            contentDescription = movie.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 0.6f
                                    )
                                )
                        )

                        feedbackIcon?.let { icon ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = feedbackAlpha.value * 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = feedbackColor.copy(alpha = feedbackAlpha.value),
                                    modifier = Modifier
                                        .size(72.dp)
                                        .graphicsLayer(
                                            scaleX = feedbackScale.value,
                                            scaleY = feedbackScale.value,
                                            alpha = feedbackAlpha.value
                                        )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.TopEnd),
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, primaryColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", movie.rating ?: 0.0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = movie.releaseDate?.take(4) ?: "",
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )

                        MovieReelsActions(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            onToWatchClick = onToWatchClick,
                            onWatchedClick = onWatchedClick,
                            onDeleteClick = onDeleteClick,
                            isToWatch = isToWatch,
                            isWatched = isWatched
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ShimmerPosterCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Tentar Novamente", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
