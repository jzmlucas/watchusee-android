package br.com.watchusee.android.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.HomeSkeleton
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.ui.components.MovieReelsActions
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.HomeUiState
import br.com.watchusee.android.viewmodel.HomeViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Long) -> Unit,
    onRequireLogin: (() -> Unit) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "WATCHUSEE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                viewModel.loadFeaturedMovie(isRefresh = true)
            },
            modifier = Modifier.weight(1f)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    if (initialState is HomeUiState.Success && targetState is HomeUiState.Success) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                },
                contentKey = { state ->
                    when (state) {
                        is HomeUiState.Success -> "success"
                        else -> state.javaClass.simpleName
                    }
                },
                label = "home_content",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                when (state) {
                    is HomeUiState.Loading -> HomeSkeleton()
                    is HomeUiState.Error -> ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadFeaturedMovie() }
                    )
                    is HomeUiState.Empty -> EmptyState(
                        message = "Nenhum filme em destaque no momento."
                    )
                    is HomeUiState.Success -> {
                        FeaturedMovieLayout(
                            movie = state.movie,
                            highlights = state.highlights,
                            isToWatch = state.status.toWatch,
                            isWatched = state.status.watched,
                            onMovieClick = onMovieClick,
                            onDetailsClick = { onMovieClick(state.movie.id) },
                            onAddClick = {
                                if (authViewModel.isAuthenticated()) {
                                    viewModel.toggleToWatchlist(state.movie.id)
                                } else {
                                    onRequireLogin { viewModel.toggleToWatchlist(state.movie.id) }
                                }
                            },
                            onWatchedClick = {
                                if (authViewModel.isAuthenticated()) {
                                    viewModel.toggleWatched(state.movie.id)
                                } else {
                                    onRequireLogin { viewModel.toggleWatched(state.movie.id) }
                                }
                            },
                            onLoadMoreHighlights = { viewModel.loadMoreHighlights() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedMovieLayout(
    movie: MovieResponse,
    highlights: List<MovieResponse>,
    isToWatch: Boolean,
    isWatched: Boolean,
    onMovieClick: (Long) -> Unit,
    onDetailsClick: () -> Unit,
    onAddClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onLoadMoreHighlights: () -> Unit
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "hero_zoom")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        ) {
            AsyncImage(
                model = TmdbImageUrl.getBackdropUrl(movie.backdropPath) ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "original"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    ),
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
                                MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.background
                            ),
                            startY = 0f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .padding(end = 64.dp) // Leave space for reels actions
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", movie.rating ?: 0.0),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = movie.releaseDate?.take(4) ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            MovieReelsActions(
                modifier = Modifier.align(Alignment.BottomEnd),
                onDetailsClick = onDetailsClick,
                onToWatchClick = onAddClick,
                onWatchedClick = onWatchedClick,
                isToWatch = isToWatch,
                isWatched = isWatched,
                isLarge = true
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "SINOPSE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (movie.overview.isNullOrBlank()) "Sinopse indisponível" else movie.overview,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )
        }

        if (highlights.isNotEmpty()) {
            HighlightsCarousel(
                highlights = highlights,
                onMovieClick = onMovieClick,
                onLoadMore = onLoadMoreHighlights
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun HighlightsCarousel(
    highlights: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    if (highlights.isEmpty()) return

    val virtualPageCount = Int.MAX_VALUE
    val startPage = remember {
        (virtualPageCount / 2) - ((virtualPageCount / 2) % highlights.size)
    }

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )

    var lastSize by remember { mutableStateOf(highlights.size) }
    LaunchedEffect(highlights.size) {
        val previousSize = lastSize
        if (previousSize > 0 && highlights.size != previousSize) {
            val currentRealIndex = pagerState.currentPage % previousSize
            val block = pagerState.currentPage / previousSize
            val adjustedPage = block * highlights.size + currentRealIndex
            pagerState.scrollToPage(adjustedPage)
        }
        lastSize = highlights.size
    }

    LaunchedEffect(highlights.size) {
        while (true) {
            delay(5.seconds)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    var lastRequestedAt by remember { mutableStateOf(-1) }
    LaunchedEffect(pagerState.currentPage, highlights.size) {
        val realIndex = pagerState.currentPage % highlights.size
        if (realIndex >= highlights.size - 2 && lastRequestedAt != highlights.size) {
            lastRequestedAt = highlights.size
            onLoadMore()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "OUTROS DESTAQUES",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val realIndex = page % highlights.size
            val movie = highlights[realIndex]
            HighlightCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) }
            )
        }
    }
}

@Composable
private fun HighlightCard(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = TmdbImageUrl.getBackdropUrl(movie.backdropPath) ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 0.5f
                        )
                    )
            )

            Text(
                text = movie.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}