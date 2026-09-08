package br.com.watchusee.android.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.ui.components.DetailSkeleton
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.DetailUiState
import br.com.watchusee.android.viewmodel.DetailViewModel
import br.com.watchusee.android.viewmodel.ShareActionState
import br.com.watchusee.android.viewmodel.ShareViewModel
import coil3.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch
import java.util.Locale

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

    var showShareDialog by rememberSaveable { mutableStateOf(false) }
    var currentMovieId by rememberSaveable(movieId) { mutableStateOf(movieId) }

    LaunchedEffect(currentMovieId) {
        viewModel.loadMovieDetail(currentMovieId)
    }

    LaunchedEffect(shareActionState) {
        when (val action = shareActionState) {
            is ShareActionState.Success -> {
                showShareDialog = false

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Filme compartilhado com sucesso! 🎉",
                        duration = SnackbarDuration.Short
                    )
                }

                shareViewModel.resetActionState()
            }

            is ShareActionState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = action.message,
                        duration = SnackbarDuration.Short
                    )
                }

                shareViewModel.resetActionState()
            }

            else -> Unit
        }
    }

    Scaffold(
        containerColor = br.com.watchusee.android.ui.theme.DarkBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    DetailSkeleton()
                }

                is DetailUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = {
                            viewModel.loadMovieDetail(currentMovieId)
                        }
                    )
                }

                is DetailUiState.Success -> {
                    MovieDetailContent(
                        state = state,
                        onBack = onBack,
                        onToggleToWatch = {
                            if (authViewModel.isAuthenticated()) {
                                viewModel.toggleToWatch(
                                    currentMovieId,
                                    state.status.toWatch
                                )

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (state.status.toWatch) {
                                            "Removido da sua lista"
                                        } else {
                                            "Adicionado à sua lista ✨"
                                        },
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onRequireLogin {
                                    viewModel.toggleToWatch(
                                        currentMovieId,
                                        state.status.toWatch
                                    )
                                }
                            }
                        },
                        onToggleWatched = {
                            if (authViewModel.isAuthenticated()) {
                                viewModel.toggleWatched(
                                    currentMovieId,
                                    state.status.watched
                                )

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (state.status.watched) {
                                            "Marcado como não assistido"
                                        } else {
                                            "Marcado como assistido ✅"
                                        },
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onRequireLogin {
                                    viewModel.toggleWatched(
                                        currentMovieId,
                                        state.status.watched
                                    )
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
                    onDismiss = {
                        showShareDialog = false
                    },
                    onShare = { nick, message ->
                        shareViewModel.createShare(
                            currentMovieId,
                            nick,
                            message
                        )
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
    onBack: () -> Unit,
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

    val overview = movie.overview
        ?.takeIf { it.isNotBlank() }
        ?: "Sinopse indisponível."

    val runtimeText = remember(movie.runtime) {
        movie.runtime?.let {
            val hours = it / 60
            val minutes = it % 60

            when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
                hours > 0 -> "${hours}h"
                else -> "${minutes}min"
            }
        } ?: ""
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(
                modifier = Modifier.height(0.dp)
            )

            HeroHeader(
                movie = movie,
                runtimeText = runtimeText
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ActionButtonsRow(
                    isInList = status.toWatch,
                    isWatched = status.watched,
                    onToggleToWatch = onToggleToWatch,
                    onToggleWatched = onToggleWatched,
                    onShareClick = onShareClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(
                    title = "Sobre o filme"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = overview,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        color = br.com.watchusee.android.ui.theme.TextGrey
                    )
                }

                movie.tagline
                    ?.takeIf { it.isNotBlank() }
                    ?.let { tagline ->
                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = br.com.watchusee.android.ui.theme.PremiumGold.copy(
                                alpha = 0.08f
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                br.com.watchusee.android.ui.theme.PremiumGold.copy(
                                    alpha = 0.15f
                                )
                            )
                        ) {
                            Text(
                                text = "\"$tagline\"",
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 22.sp,
                                color = br.com.watchusee.android.ui.theme.PremiumGold.copy(
                                    alpha = 0.9f
                                )
                            )
                        }
                    }

                Spacer(modifier = Modifier.height(32.dp))

                AdditionalInfoSection(
                    movie = movie
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            state.trailer?.let {
                TrailerSection(
                    trailer = it
                )
            }

            if (state.recommendations.isNotEmpty()) {
                RelatedMoviesSection(
                    title = "VOCÊ TAMBÉM PODE GOSTAR",
                    movies = state.recommendations,
                    onMovieClick = onMovieClick
                )
            }

            if (relatedMovies.isNotEmpty()) {
                RelatedMoviesSection(
                    title = "OUTRAS RECOMENDAÇÕES",
                    movies = relatedMovies,
                    onMovieClick = onMovieClick,
                    onLoadMore = onLoadMoreRelated
                )
            }

            if (state.reviews.isNotEmpty()) {
                ReviewsSection(
                    reviews = state.reviews
                )
            }

            Spacer(
                modifier = Modifier.height(100.dp)
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    top = 10.dp,
                    start = 12.dp
                )
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = CircleShape
                )
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Voltar",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun AdditionalInfoSection(
    movie: br.com.watchusee.android.data.dto.MovieDetailsResponse
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionTitle(
            title = "Informações"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                InfoRow(
                    label = "Título original",
                    value = movie.originalTitle ?: movie.title
                )

                InfoRow(
                    label = "Status",
                    value = movie.status ?: "Desconhecido"
                )

                movie.releaseYear?.let {
                    InfoRow(
                        label = "Ano de lançamento",
                        value = it.toString()
                    )
                }

                movie.originalLanguage
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        InfoRow(
                            label = "Idioma original",
                            value = it.uppercase()
                        )
                    }

                if (movie.budget != null && movie.budget > 0) {
                    InfoRow(
                        label = "Orçamento",
                        value = "$${String.format(Locale.US, "%,d", movie.budget)}"
                    )
                }

                if (movie.revenue != null && movie.revenue > 0) {
                    InfoRow(
                        label = "Receita",
                        value = "$${String.format(Locale.US, "%,d", movie.revenue)}"
                    )
                }

                if (!movie.productionCompanies.isNullOrEmpty()) {
                    InfoRow(
                        label = "Produção",
                        value = movie.productionCompanies.joinToString(", ") {
                            it.name
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(132.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = br.com.watchusee.android.ui.theme.TextWhite.copy(
                alpha = 0.55f
            ),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "—",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = br.com.watchusee.android.ui.theme.TextWhite.copy(
                alpha = 0.25f
            ),
            fontSize = 12.sp
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = br.com.watchusee.android.ui.theme.TextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = br.com.watchusee.android.ui.theme.TextWhite,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun HeroHeader(
    movie: br.com.watchusee.android.data.dto.MovieDetailsResponse,
    runtimeText: String
) {
    val imageUrl = remember(
        movie.backdropPath,
        movie.posterPath
    ) {
        TmdbImageUrl.getBackdropUrl(
            movie.backdropPath
        ) ?: TmdbImageUrl.getPosterUrl(
            movie.posterPath,
            "w780"
        )
    }

    val posterUrl = remember(
        movie.posterPath
    ) {
        TmdbImageUrl.getPosterUrl(
            movie.posterPath,
            "w342"
        )
    }

    val genres = movie.genres ?: emptyList()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                br.com.watchusee.android.ui.theme.DarkBackground.copy(
                                    alpha = 0.7f
                                ),
                                br.com.watchusee.android.ui.theme.DarkBackground
                            )
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 20.dp,
                        end = 16.dp
                    ),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = br.com.watchusee.android.ui.theme.PremiumGold,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text(
                        text = if ((movie.rating ?: 0.0) > 0.0) {
                            String.format(
                                Locale.US,
                                "%.1f",
                                movie.rating
                            )
                        } else {
                            "—"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-58).dp)
                .padding(
                    horizontal = 16.dp
                ),
            verticalAlignment = Alignment.Bottom
        ) {
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = br.com.watchusee.android.ui.theme.TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 28.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    movie.releaseYear?.let {
                        Text(
                            text = it.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = br.com.watchusee.android.ui.theme.TextGrey
                        )
                    }

                    if (runtimeText.isNotEmpty()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = br.com.watchusee.android.ui.theme.TextGrey
                        )

                        Text(
                            text = runtimeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = br.com.watchusee.android.ui.theme.TextGrey
                        )
                    }

                    movie.voteCount?.let {
                        if (it > 0) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = br.com.watchusee.android.ui.theme.TextGrey
                            )

                            Text(
                                text = "${it} avaliações",
                                style = MaterialTheme.typography.bodySmall,
                                color = br.com.watchusee.android.ui.theme.TextGrey
                            )
                        }
                    }
                }
            }
        }

        if (genres.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-38).dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(genres.size) { index ->
                    val genre = genres[index]

                    Surface(
                        color = if (index == 0) {
                            br.com.watchusee.android.ui.theme.PremiumGold
                        } else {
                            br.com.watchusee.android.ui.theme.SurfaceGrey
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = if (index == 0) {
                            null
                        } else {
                            BorderStroke(
                                1.dp,
                                br.com.watchusee.android.ui.theme.GraySubtle
                            )
                        }
                    ) {
                        Text(
                            text = genre.name.uppercase(),
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (index == 0) {
                                Color.Black
                            } else {
                                br.com.watchusee.android.ui.theme.TextWhite
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    isInList: Boolean,
    isWatched: Boolean,
    onToggleToWatch: () -> Unit,
    onToggleWatched: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                icon = if (isInList) {
                    Icons.Rounded.Bookmark
                } else {
                    Icons.Rounded.BookmarkBorder
                },
                text = if (isInList) {
                    "Na lista"
                } else {
                    "Assistir"
                },
                active = isInList,
                onClick = onToggleToWatch
            )

            ActionButton(
                modifier = Modifier.weight(1f),
                icon = if (isWatched) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.Check
                },
                text = if (isWatched) {
                    "Assistido"
                } else {
                    "Marcar visto"
                },
                active = isWatched,
                onClick = onToggleWatched
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                1.dp,
                br.com.watchusee.android.ui.theme.PremiumGold.copy(
                    alpha = 0.7f
                )
            ),
            contentPadding = PaddingValues(
                horizontal = 16.dp
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = br.com.watchusee.android.ui.theme.PremiumGold
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = "Compartilhar com amigo",
                color = br.com.watchusee.android.ui.theme.PremiumGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) {
                br.com.watchusee.android.ui.theme.PremiumGold
            } else {
                br.com.watchusee.android.ui.theme.SurfaceGrey
            },
            contentColor = if (active) {
                Color.Black
            } else {
                br.com.watchusee.android.ui.theme.TextWhite
            }
        ),
        contentPadding = PaddingValues(
            horizontal = 10.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TrailerSection(
    trailer: br.com.watchusee.android.data.dto.MovieTrailerResponse
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
    ) {
        SectionTitle(
            title = "Trailer"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        val lifecycleOwner = LocalLifecycleOwner.current

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    YouTubePlayerView(context).apply {
                        lifecycleOwner.lifecycle.addObserver(this)

                        addYouTubePlayerListener(
                            object : AbstractYouTubePlayerListener() {
                                override fun onReady(
                                    youTubePlayer: YouTubePlayer
                                ) {
                                    youTubePlayer.cueVideo(
                                        trailer.key,
                                        0f
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RelatedMoviesSection(
    title: String,
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    onLoadMore: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    var lastRequestedSize by remember {
        mutableStateOf(-1)
    }

    LaunchedEffect(
        listState,
        movies.size
    ) {
        snapshotFlow {
            listState.layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->
            if (
                lastVisibleIndex != null &&
                movies.isNotEmpty() &&
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
            .padding(vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = br.com.watchusee.android.ui.theme.TextWhite
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(movies.size) { index ->
                val movie = movies[index]

                RelatedMovieCard(
                    movie = movie,
                    onClick = {
                        onMovieClick(movie.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<br.com.watchusee.android.data.dto.ReviewResponse>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 18.dp
            )
    ) {
        SectionTitle(
            title = "Avaliações da comunidade"
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        reviews
            .take(3)
            .forEachIndexed { index, review ->
                ReviewItem(
                    review = review
                )

                if (index < reviews.take(3).lastIndex) {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }
    }
}

@Composable
private fun ReviewItem(
    review: br.com.watchusee.android.data.dto.ReviewResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                    color = br.com.watchusee.android.ui.theme.PremiumGold.copy(
                        alpha = 0.2f
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.author
                                .take(1)
                                .uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = br.com.watchusee.android.ui.theme.PremiumGold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = review.author,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = br.com.watchusee.android.ui.theme.TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Avaliação da comunidade",
                        style = MaterialTheme.typography.labelSmall,
                        color = br.com.watchusee.android.ui.theme.TextGrey
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = br.com.watchusee.android.ui.theme.TextGrey,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RelatedMovieCard(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .scaleOnClick(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = TmdbImageUrl.getPosterUrl(
                        movie.posterPath,
                        "w342"
                    ),
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = movie.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = br.com.watchusee.android.ui.theme.TextWhite,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ShareMovieDialog(
    onDismiss: () -> Unit,
    onShare: (String, String?) -> Unit,
    isLoading: Boolean
) {
    var recipientNick by rememberSaveable {
        mutableStateOf("")
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    tint = br.com.watchusee.android.ui.theme.PremiumGold,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = "Compartilhar filme",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Envie este filme para um amigo do WatchUsee. 💬",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = recipientNick,
                    onValueChange = {
                        recipientNick = it
                    },
                    label = {
                        Text("Nick do destinatário")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        if (recipientNick.isBlank()) {
                            Text(
                                text = "Campo obrigatório",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                    },
                    label = {
                        Text("Mensagem (opcional)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onShare(
                        recipientNick.trim(),
                        message
                            .trim()
                            .takeIf {
                                it.isNotBlank()
                            }
                    )
                },
                enabled = recipientNick.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = br.com.watchusee.android.ui.theme.PremiumGold
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Compartilhar",
                        color = Color.Black
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}