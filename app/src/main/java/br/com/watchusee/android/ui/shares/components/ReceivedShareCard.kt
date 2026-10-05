package br.com.watchusee.android.ui.shares.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.ShareResponse
import br.com.watchusee.android.data.dto.ShareStatus
import br.com.watchusee.android.util.TmdbImageUrl
import coil3.compose.AsyncImage

@Composable
fun ReceivedShareCard(
    share: ShareResponse,
    movie: MovieResponse?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMovieClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            SharePersonHeader(share = share)

            if (movie != null) {
                MoviePreview(movie = movie, onClick = onMovieClick)
            } else {
                MovieLoadingPlaceholder()
            }

            if (!share.message.isNullOrBlank()) {
                ShareMessage(message = share.message)
            }

            if (share.status == ShareStatus.PENDING) {
                ShareActions(onReject = onReject, onAccept = onAccept)
            }
        }
    }
}

@Composable
private fun SharePersonHeader(share: ShareResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = share.senderNick,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "recomendou um filme para você",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                // ⚠️ Troque createdAt pelo nome real no seu ShareResponse
                text = formatShareDate(share.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }

        Spacer(Modifier.width(8.dp))
        StatusBadge(status = share.status)
    }
}

@Composable
private fun MoviePreview(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        val imageUrl = TmdbImageUrl.getBackdropUrl(movie.backdropPath)
            ?: TmdbImageUrl.getPosterUrl(movie.posterPath)

        AsyncImage(
            model = imageUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.90f)
                        ),
                        startY = 70f
                    )
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = "Ver detalhes",
                modifier = Modifier.padding(8.dp).size(18.dp),
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Movie,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White.copy(alpha = 0.80f)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Ver detalhes do filme",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun MovieLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun ShareMessage(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u201C",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShareActions(
    onReject: () -> Unit,
    onAccept: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Recusar", fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onAccept,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Aceitar", fontWeight = FontWeight.Bold)
        }
    }
}