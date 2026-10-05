package br.com.watchusee.android.ui.cinema

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.PersonAddAlt1
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.theme.CinemaAccent
import br.com.watchusee.android.ui.theme.CinemaCanvas
import br.com.watchusee.android.ui.theme.CinemaMuted
import br.com.watchusee.android.ui.theme.CinemaOlive
import br.com.watchusee.android.ui.theme.CinemaPanel
import br.com.watchusee.android.ui.theme.CinemaText
import br.com.watchusee.android.util.TmdbImageUrl
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun PosterCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    width: androidx.compose.ui.unit.Dp = 132.dp
) {
    Column(modifier = modifier.width(width)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(CinemaPanel)
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
                contentDescription = "Pôster de ${movie.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (onFavoriteClick != null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CinemaCanvas.copy(alpha = 0.82f))
                        .clickable(onClick = onFavoriteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                        tint = if (isFavorite) CinemaAccent else CinemaText,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            movie.rating?.takeIf { it > 0 }?.let { rating ->
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    color = CinemaCanvas.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Star, null, tint = CinemaAccent, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${"%.1f".format(rating)}", color = CinemaText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = movie.title,
            color = CinemaText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        movie.releaseDate?.take(4)?.let {
            Text(it, color = CinemaMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun PosterRail(
    title: String,
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            title,
            color = CinemaText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                PosterCard(movie = movie, onClick = { onMovieClick(movie.id) })
            }
        }
    }
}

@Composable
fun StarRating(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var dragValue by remember(value) { mutableFloatStateOf(value) }
    Row(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        val next = ((offset.x / size.width) * 5f).coerceIn(0f, 5f)
                        dragValue = (next * 2).roundToInt() / 2f
                        onValueChange(dragValue)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val next = ((change.position.x / size.width) * 5f).coerceIn(0f, 5f)
                        val rounded = (next * 2).roundToInt() / 2f
                        if (rounded != dragValue) {
                            dragValue = rounded
                            onValueChange(rounded)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onDragEnd = {}
                )
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val fill = (value - index).coerceIn(0f, 1f)
            StarGlyph(fill = fill, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
private fun StarGlyph(fill: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val star = Path().apply {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension / 2f
            val inner = outer * 0.45f
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) outer else inner
                val angle = Math.toRadians(-90.0 + i * 36.0)
                val point = androidx.compose.ui.geometry.Offset(
                    center.x + (radius * kotlin.math.cos(angle)).toFloat(),
                    center.y + (radius * kotlin.math.sin(angle)).toFloat()
                )
                if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(star, CinemaMuted.copy(alpha = 0.35f))
        if (fill > 0f) {
            drawPath(star, CinemaAccent.copy(alpha = fill))
        }
        drawPath(star, CinemaAccent.copy(alpha = 0.85f), style = Stroke(width = 1.5.dp.toPx()))
    }
}

data class FriendActivity(
    val userName: String,
    val handle: String,
    val action: String,
    val movie: MovieResponse,
    val review: String,
    val rating: Float,
    val minutesAgo: Int
)

@Composable
fun FriendActivityItem(
    activity: FriendActivity,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CinemaOlive),
                contentAlignment = Alignment.Center
            ) {
                Text(activity.userName.take(1), color = CinemaCanvas, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.userName, color = CinemaText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${activity.action} · há ${activity.minutesAgo} min", color = CinemaMuted, fontSize = 12.sp)
            }
            Text("@${activity.handle}", color = CinemaMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CinemaPanel).clickable { onMovieClick(activity.movie.id) }.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosterCard(activity.movie, onClick = { onMovieClick(activity.movie.id) }, width = 70.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.movie.title, color = CinemaText, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                StarRating(activity.rating, onValueChange = {}, enabled = false, modifier = Modifier.height(22.dp))
                Spacer(Modifier.height(6.dp))
                Text("\"${activity.review}\"", color = CinemaMuted, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ListCover(movies: List<MovieResponse>, modifier: Modifier = Modifier) {
    val coverMovies = movies.take(4)
    Box(modifier = modifier.aspectRatio(1.45f).clip(RoundedCornerShape(12.dp)).background(CinemaPanel)) {
        coverMovies.forEachIndexed { index, movie ->
            AsyncImage(
                model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxSize()
                    .padding(if (coverMovies.size > 1) 1.dp else 0.dp)
                    .align(if (index % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd),
                contentScale = ContentScale.Crop
            )
        }
        if (coverMovies.isEmpty()) Icon(Icons.Rounded.PlaylistAdd, null, tint = CinemaMuted, modifier = Modifier.align(Alignment.Center).size(32.dp))
    }
}

@Composable
fun ProfileHeader(
    name: String,
    handle: String,
    bio: String,
    onAddFriend: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(CinemaOlive), contentAlignment = Alignment.Center) {
            Text(name.take(1), color = CinemaCanvas, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = CinemaText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("@$handle", color = CinemaAccent, fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
            Text(bio, color = CinemaMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        onAddFriend?.let {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(CinemaAccent).clickable { it() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PersonAddAlt1, "Adicionar amigo", tint = CinemaCanvas)
            }
        }
    }
}

@Composable
fun ShareCard(
    movie: MovieResponse,
    rating: Float,
    quote: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CinemaPanel).padding(20.dp)) {
        Text("WATCHUSEE", color = CinemaAccent, fontWeight = FontWeight.Black, letterSpacing = 1.sp, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PosterCard(movie, onClick = {}, width = 110.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(movie.title, color = CinemaText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                StarRating(rating, {}, enabled = false)
                Spacer(Modifier.height(10.dp))
                Text("\"$quote\"", color = CinemaMuted, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Uma nota do seu diário de cinema", color = CinemaMuted, fontSize = 11.sp)
    }
}
