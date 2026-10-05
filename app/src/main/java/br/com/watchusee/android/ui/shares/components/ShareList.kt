package br.com.watchusee.android.ui.shares.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.viewmodel.ShareUiState

@Composable
fun ShareList(
    state: ShareUiState,
    movieDetails: Map<Long, MovieResponse>,
    isReceived: Boolean,
    onMovieClick: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onAccept: (Long) -> Unit = {},
    onReject: (Long) -> Unit = {}
) {
    when (state) {
        is ShareUiState.Loading -> LoadingState()

        is ShareUiState.Error -> ErrorState(
            message = state.message,
            onRetry = onRetry
        )

        is ShareUiState.Success -> {
            if (state.shares.isEmpty()) {
                EmptySharesState(isReceived = isReceived, modifier = modifier)
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = state.shares, key = { it.id }) { share ->
                        if (isReceived) {
                            ReceivedShareCard(
                                share = share,
                                movie = movieDetails[share.movieId],
                                onAccept = { onAccept(share.id) },
                                onReject = { onReject(share.id) },
                                onMovieClick = { onMovieClick(share.movieId) }
                            )
                        } else {
                            ShareListItem(
                                share = share,
                                movie = movieDetails[share.movieId],
                                onMovieClick = onMovieClick
                            )
                        }
                    }
                }
            }
        }

        else -> LoadingState()
    }
}

@Composable
private fun EmptySharesState(
    isReceived: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isReceived) {
                            Icons.Rounded.MailOutline
                        } else {
                            Icons.AutoMirrored.Rounded.Send
                        },
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = if (isReceived) {
                    "Sua caixa está vazia"
                } else {
                    "Nenhuma recomendação ainda"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isReceived) {
                    "Quando um amigo compartilhar um filme\ncom você, ele aparecerá aqui."
                } else {
                    "Compartilhe um filme com seus amigos\ne comece uma conversa."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}