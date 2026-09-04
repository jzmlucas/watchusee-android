package br.com.watchusee.android.ui.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.components.DetailSkeleton
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.DetailUiState
import br.com.watchusee.android.viewmodel.DetailViewModel
import br.com.watchusee.android.viewmodel.ShareActionState
import br.com.watchusee.android.viewmodel.ShareViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBack: () -> Unit,
    onRequireLogin: (() -> Unit) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel(),
    shareViewModel: ShareViewModel = hiltViewModel(),
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareActionState by shareViewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBackButton by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    var currentMovieId by rememberSaveable(movieId) { mutableStateOf(movieId) }

    LaunchedEffect(currentMovieId) {
        viewModel.loadMovieDetail(currentMovieId)
        delay(200)
        showBackButton = true
    }

    LaunchedEffect(shareActionState) {
        if (shareActionState is ShareActionState.Success) {
            showShareDialog = false
            scope.launch {
                snackbarHostState.showSnackbar("Filme compartilhado com sucesso!")
            }
            shareViewModel.resetActionState()
        } else if (shareActionState is ShareActionState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((shareActionState as ShareActionState.Error).message)
            }
            shareViewModel.resetActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "WATCHUSEE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> DetailSkeleton()
                is DetailUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadMovieDetail(currentMovieId) }
                )
                is DetailUiState.Success -> {
                    MovieDetailContent(
                        state = state,
                        onToggleToWatch = {
                            if (authViewModel.isAuthenticated()) {
                                viewModel.toggleToWatch(currentMovieId, state.status.toWatch)
                                scope.launch {
                                    val message = if (state.status.toWatch) {
                                        "Removido da sua lista"
                                    } else {
                                        "Adicionado à sua lista"
                                    }
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onRequireLogin {
                                    viewModel.toggleToWatch(currentMovieId, state.status.toWatch)
                                }
                            }
                        },
                        onToggleWatched = {
                            if (authViewModel.isAuthenticated()) {
                                viewModel.toggleWatched(currentMovieId, state.status.watched)
                                scope.launch {
                                    val message = if (state.status.watched) {
                                        "Marcado como não assistido"
                                    } else {
                                        "Marcado como assistido"
                                    }
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onRequireLogin {
                                    viewModel.toggleWatched(currentMovieId, state.status.watched)
                                }
                            }
                        },
                        onShareClick = {
                            if (authViewModel.isAuthenticated()) {
                                showShareDialog = true
                            } else {
                                onRequireLogin {
                                    showShareDialog = true
                                }
                            }
                        },
                        onMovieClick = { id ->
                            currentMovieId = id
                        },
                        onLoadMoreRelated = {
                            viewModel.loadMoreSimilarMovies(currentMovieId)
                        }
                    )
                }
            }

            if (showShareDialog) {
                ShareMovieDialog(
                    onDismiss = { showShareDialog = false },
                    onShare = { nick, message ->
                        shareViewModel.createShare(currentMovieId, nick, message)
                    },
                    isLoading = shareActionState is ShareActionState.Loading
                )
            }
        }
    }
}

@Composable
private fun MovieDetailContent(
    state: DetailUiState.Success,
    onToggleToWatch: () -> Unit,
    onToggleWatched: () -> Unit,
    onShareClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onLoadMoreRelated: () -> Unit
) {
    val movie = state.movie
    val status = state.status
    val relatedMovies = state.relatedMovies
    val scrollState = rememberScrollState()
    var showFullOverview by rememberSaveable { mutableStateOf(false) }
    val overview = if (movie.overview.isNullOrBlank()) "Sinopse indisponível" else movie.overview
    val shouldShowExpandButton = overview.length > 150

    var feedbackIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var feedbackColor by remember { mutableStateOf(Color.White) }
    val feedbackAlpha = remember { Animatable(0f) }
    val feedbackScale = remember { Animatable(0.5f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()

    var prevToWatch by remember { mutableStateOf(status.toWatch) }
    var prevWatched by remember { mutableStateOf(status.watched) }

    LaunchedEffect(status.toWatch, status.watched) {
        val isToWatch = status.toWatch
        val isWatched = status.watched
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
                else -> Color.Red
            }

            scope.launch {
                feedbackAlpha.snapTo(0.8f)
                feedbackScale.snapTo(0.6f)
                launch { feedbackAlpha.animateTo(0f, tween(800)) }
                launch { feedbackScale.animateTo(1.5f, tween(800)) }
            }
        }
        prevToWatch = isToWatch
        prevWatched = isWatched
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            val imageUrl = remember(movie.backdropPath, movie.posterPath) {
                TmdbImageUrl.getBackdropUrl(movie.backdropPath)
                    ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780")
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = null,
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
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            feedbackIcon?.let { icon ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = feedbackColor.copy(alpha = feedbackAlpha.value),
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer(
                                scaleX = feedbackScale.value,
                                scaleY = feedbackScale.value,
                                alpha = feedbackAlpha.value
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%.1f", movie.rating ?: 0.0),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = movie.releaseDate?.take(4) ?: "N/A",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onToggleToWatch,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status.toWatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (status.toWatch) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    if (status.toWatch) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (status.toWatch) "Na Lista" else "Lista",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onToggleWatched,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status.watched) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (status.watched) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    if (status.watched) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (status.watched) "Assistido" else "Assistido",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Compartilhar com Amigo",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                text = "SINOPSE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (showFullOverview) overview else overview.take(150) + if (overview.length > 150) "..." else "",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            if (shouldShowExpandButton) {
                TextButton(
                    onClick = { showFullOverview = !showFullOverview },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        if (showFullOverview) "Ver menos" else "Ver mais",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        TrailerSection(trailer = state.trailer)

        if (relatedMovies.isNotEmpty()) {
            RelatedMoviesSection(
                movies = relatedMovies,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMoreRelated
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun TrailerSection(trailer: br.com.watchusee.android.data.dto.MovieTrailerResponse?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Text(
            text = "TRAILER",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (trailer != null) {
            val lifecycleOwner = LocalLifecycleOwner.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        YouTubePlayerView(context).apply {
                            lifecycleOwner.lifecycle.addObserver(this)
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    youTubePlayer.cueVideo(trailer.key, 0f)
                                }
                            })
                        }
                    }
                )
            }
        } else {
            Text(
                text = "Trailer indisponível",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RelatedMoviesSection(
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    var lastRequestedSize by remember { mutableStateOf(-1) }

    LaunchedEffect(listState, movies.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= movies.size - 3 &&
                    lastRequestedSize != movies.size
                ) {
                    lastRequestedSize = movies.size
                    onLoadMore()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "VOCÊ TAMBÉM PODE GOSTAR",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies.size) { index ->
                val movie = movies[index]
                RelatedMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) }
                )
            }
        }
    }
}

@Composable
private fun RelatedMovieCard(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 0.6f
                        )
                    )
            )
        }
    }
}

@Composable
fun ShareMovieDialog(
    onDismiss: () -> Unit,
    onShare: (String, String?) -> Unit,
    isLoading: Boolean
) {
    var recipientNick by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartilhar Filme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Informe o nick do seu amigo para compartilhar este filme.")
                OutlinedTextField(
                    value = recipientNick,
                    onValueChange = { recipientNick = it },
                    label = { Text("Nick do Destinatário") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mensagem (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onShare(recipientNick, message.takeIf { it.isNotBlank() }) },
                enabled = recipientNick.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Compartilhar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}