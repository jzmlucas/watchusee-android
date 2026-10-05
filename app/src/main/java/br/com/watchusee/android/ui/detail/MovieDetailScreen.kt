package br.com.watchusee.android.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieCastMemberResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.components.DetailSkeleton
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.*
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit,
    onCastClick: () -> Unit = {},
    onRequireLogin: (() -> Unit) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel(),
    shareViewModel: ShareViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareActionState by shareViewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showShareDialog by rememberSaveable { mutableStateOf(false) }
    var currentMovieId by rememberSaveable(movieId) { mutableStateOf(movieId) }

    LaunchedEffect(currentMovieId) { viewModel.loadMovieDetail(currentMovieId) }
    LaunchedEffect(shareActionState) {
        when (val action = shareActionState) {
            is ShareActionState.Success -> { showShareDialog = false; scope.launch { snackbarHostState.showSnackbar("Filme compartilhado com sucesso!") }; shareViewModel.resetActionState() }
            is ShareActionState.Error -> { scope.launch { snackbarHostState.showSnackbar(action.message) }; shareViewModel.resetActionState() }
            else -> Unit
        }
    }

    Scaffold(containerColor = CinemaCanvas, snackbarHost = { SnackbarHost(snackbarHostState) }) {
        when (val state = uiState) {
            DetailUiState.Loading -> DetailSkeleton()
            is DetailUiState.Error -> ErrorState(state.message, onRetry = { viewModel.loadMovieDetail(currentMovieId) })
            is DetailUiState.Success -> DetailContent(
                state = state,
                onBack = onBack,
                onShare = { if (authViewModel.isAuthenticated()) showShareDialog = true else onRequireLogin { showShareDialog = true } },
                onToggleToWatch = { if (authViewModel.isAuthenticated()) viewModel.toggleToWatch(currentMovieId, state.status.toWatch) else onRequireLogin { viewModel.toggleToWatch(currentMovieId, state.status.toWatch) } },
                onToggleWatched = { if (authViewModel.isAuthenticated()) viewModel.toggleWatched(currentMovieId, state.status.watched) else onRequireLogin { viewModel.toggleWatched(currentMovieId, state.status.watched) } },
                onMovieClick = { currentMovieId = it },
                onCastClick = onCastClick
            )
        }
    }

    if (showShareDialog) {
        ShareMovieDialog(
            onDismiss = { showShareDialog = false },
            onShare = { nick, message -> shareViewModel.createShare(currentMovieId, nick, message) },
            isLoading = shareActionState is ShareActionState.Loading
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Success,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onToggleToWatch: () -> Unit,
    onToggleWatched: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onCastClick: () -> Unit
) {
    val movie = state.movie
    val uriHandler = LocalUriHandler.current
    val trailerUrl = state.trailer?.takeIf { it.key.isNotBlank() }?.let { trailer ->
        if (trailer.site.equals("YouTube", ignoreCase = true)) {
            "https://www.youtube.com/watch?v=${trailer.key}"
        } else {
            null
        }
    }
    val metadata = listOfNotNull(movie.releaseYear?.toString(), movie.runtime?.formatRuntime(), movie.genres?.firstOrNull()?.name).joinToString("  ·  ")
    val rating = movie.rating?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.1f", it).replace('.', ',') } ?: "—"
    val voteCount = movie.voteCount?.takeIf { it > 0 }?.let(::formatCount) ?: "Sem avaliações"

    Box(Modifier.fillMaxSize().background(CinemaCanvas)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(264.dp)) {
                AsyncImage(model = TmdbImageUrl.getBackdropUrl(movie.backdropPath, "w780") ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"), contentDescription = movie.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(.12f), Color.Transparent, CinemaCanvas.copy(.92f)))))
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    CircleIcon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", onBack)
                    CircleIcon(Icons.Rounded.Share, "Compartilhar", onShare)
                }
                Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Text(movie.title, color = CinemaText, fontSize = 34.sp, lineHeight = 33.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(320.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Text(metadata.ifBlank { "Informações indisponíveis" }, color = CinemaMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (movie.adult) Text("18", color = CinemaMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 8.dp).border(1.dp, Color(0xFF5C636C), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(rating, color = CinemaAccent, fontSize = 28.sp)
                        Column(Modifier.padding(start = 8.dp)) { Text("★★★★★", color = CinemaAccent, fontSize = 10.sp); Text(voteCount, color = CinemaMuted, fontSize = 9.sp) }
                    }
                    CircleIcon(if (state.status.toWatch) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Marcar para assistir", onToggleToWatch)
                    Spacer(Modifier.width(8.dp))
                    CircleIcon(if (state.status.watched) Icons.Rounded.CheckCircle else Icons.Rounded.Check, "Já assisti", onToggleWatched)
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Sinopse", color = CinemaText, fontSize = 20.sp)
                    Text(movie.overview?.takeIf { it.isNotBlank() } ?: "Sinopse indisponível.", color = CinemaMuted, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (movie.cast.isNotEmpty()) CastSection(movie.cast, onCastClick)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { trailerUrl?.let(uriHandler::openUri) }, enabled = trailerUrl != null, modifier = Modifier.weight(1f).height(48.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = CinemaAccent, disabledContainerColor = CinemaPanelRaised), contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = CinemaCanvas, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text("Ver trailer", color = CinemaCanvas, fontSize = 13.sp)
                    }
                    OutlinedButton(onClick = onToggleToWatch, modifier = Modifier.width(112.dp).height(48.dp), shape = CircleShape, border = BorderStroke(1.dp, CinemaMuted), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Icon(if (state.status.toWatch) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, null, tint = CinemaText, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text("Salvar", color = CinemaText, fontSize = 13.sp)
                    }
                }
                RelatedRail(state.recommendations.ifEmpty { state.relatedMovies }, onMovieClick)
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun CastSection(cast: List<MovieCastMemberResponse>, onCastClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Elenco", color = CinemaText, fontSize = 20.sp, modifier = Modifier.weight(1f))
            Text("Ver todos", color = CinemaAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onCastClick))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(cast.take(4), key = { it.id }) { person ->
                Column(Modifier.width(70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(model = TmdbImageUrl.getPosterUrl(person.profilePath, "w185"), contentDescription = person.name, modifier = Modifier.size(46.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Text(person.name, color = CinemaText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
                    Text(person.character.orEmpty(), color = CinemaMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun RelatedRail(movies: List<MovieResponse>, onMovieClick: (Long) -> Unit) {
    if (movies.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Você também pode gostar", color = CinemaText, fontSize = 20.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(movies.take(10), key = { it.id }) { movie -> AsyncImage(model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"), contentDescription = movie.title, modifier = Modifier.width(104.dp).height(132.dp).clip(RoundedCornerShape(14.dp)).clickable { onMovieClick(movie.id) }, contentScale = ContentScale.Crop) }
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(CircleShape).background(CinemaPanelRaised).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, description, tint = CinemaText, modifier = Modifier.size(18.dp)) }
}

private fun Int.formatRuntime(): String { val hours = this / 60; val minutes = this % 60; return when { hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"; hours > 0 -> "${hours}h"; else -> "${minutes}min" } }
private fun formatCount(value: Int): String = when { value >= 1_000_000 -> String.format(Locale.US, "%.1f mi avaliações", value / 1_000_000.0); value >= 1_000 -> String.format(Locale.US, "%.1f mil avaliações", value / 1_000.0); else -> "$value avaliações" }

@Composable
fun ShareMovieDialog(onDismiss: () -> Unit, onShare: (String, String?) -> Unit, isLoading: Boolean) {
    var recipientNick by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = { if (!isLoading) onDismiss() }, title = { Text("Compartilhar filme") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(recipientNick, { recipientNick = it }, label = { Text("Nick do destinatário") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading, singleLine = true); OutlinedTextField(message, { message = it }, label = { Text("Mensagem (opcional)") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading, minLines = 3) } }, confirmButton = { Button(onClick = { onShare(recipientNick.trim(), message.trim().takeIf { it.isNotBlank() }) }, enabled = recipientNick.isNotBlank() && !isLoading) { Text("Compartilhar") } }, dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") } })
}
