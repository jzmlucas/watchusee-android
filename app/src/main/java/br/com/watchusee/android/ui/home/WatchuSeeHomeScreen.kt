package br.com.watchusee.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.HomeUiState
import br.com.watchusee.android.viewmodel.HomeViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun WatchuSeeHomeScreen(
    onMovieClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoggedIn = currentUser != null
    val scrollState = rememberScrollState()

    LaunchedEffect(isLoggedIn) {
        homeViewModel.loadFeaturedMovie(isRefresh = true)
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            WatchuSeeHeader(
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick,
                profileImageUrl = null
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            when (val state = homeUiState) {
                is HomeUiState.Success -> {
                    HeroCarousel(
                        movies = state.trendingMovies.take(6),
                        onMovieClick = onMovieClick,
                        watchlistStatusMap = state.watchlistStatusMap,
                        loadingMovieIds = state.loadingMovieIds,
                        isLoggedIn = isLoggedIn,
                        onToggleWatchlist = { movieId, status ->
                            homeViewModel.toggleWatchlist(movieId, status)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoggedIn && state.toWatchList.isNotEmpty()) {
                        MovieHorizontalSection(
                            title = "Para assistir",
                            movies = state.toWatchList,
                            watchlistStatusMap = state.watchlistStatusMap,
                            isLoggedIn = isLoggedIn,
                            onMovieClick = onMovieClick
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    if (isLoggedIn && state.watchedList.isNotEmpty()) {
                        MovieHorizontalSection(
                            title = "Já assistidos",
                            movies = state.watchedList,
                            watchlistStatusMap = state.watchlistStatusMap,
                            isLoggedIn = isLoggedIn,
                            onMovieClick = onMovieClick
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    MovieHorizontalSection(
                        title = "Populares",
                        movies = state.popularMovies,
                        watchlistStatusMap = state.watchlistStatusMap,
                        isLoggedIn = isLoggedIn,
                        onMovieClick = onMovieClick
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    if (state.recommendedMovies.isNotEmpty()) {
                        MovieHorizontalSection(
                            title = "Recomendados para você",
                            movies = state.recommendedMovies,
                            watchlistStatusMap = state.watchlistStatusMap,
                            isLoggedIn = isLoggedIn,
                            onMovieClick = onMovieClick
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                is HomeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.White)
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun WatchuSeeHeader(
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    profileImageUrl: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onSearchClick() },
            color = SurfaceGrey
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar filmes",
                    tint = PremiumGold,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Procurar por filmes...",
                    color = TextGrey,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, GraySubtle, CircleShape)
                .clickable { onProfileClick() },
            color = SurfaceGrey
        ) {
            AsyncImage(
                model = profileImageUrl
                    ?: "https://i.pinimg.com/564x/0e/90/ee/0e90ee3f40e3dd616d2a2e50c0c1f23e.jpg",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun HeaderIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(JetBlack.copy(alpha = 0.6f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun HeroCarousel(
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    watchlistStatusMap: Map<Long, String>,
    loadingMovieIds: Set<Long>,
    isLoggedIn: Boolean,
    onToggleWatchlist: (Long, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { movies.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(5.seconds)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % movies.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp
        ) { page ->
            val movie = movies[page]
            val status = if (isLoggedIn) watchlistStatusMap[movie.id] else null
            val isLoading = movie.id in loadingMovieIds
            
            HeroCard(
                movie = movie,
                isToWatch = status == "TO_WATCH",
                isWatched = status == "WATCHED",
                isLoading = isLoading,
                isLoggedIn = isLoggedIn,
                onToWatchClick = { onToggleWatchlist(movie.id, "TO_WATCH") },
                onWatchedClick = { onToggleWatchlist(movie.id, "WATCHED") },
                onClick = { onMovieClick(movie.id) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(movies.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) PremiumGold else TextGrey
                val width = if (pagerState.currentPage == iteration) 24.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                        .width(width)
                        .height(6.dp)
                )
            }
        }
    }
}

@Composable
fun HeroCard(
    movie: MovieResponse,
    isToWatch: Boolean,
    isWatched: Boolean,
    isLoading: Boolean,
    isLoggedIn: Boolean,
    onToWatchClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
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
                                DarkNavy.copy(alpha = 0.8f)
                            ),
                            startY = 300f
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Destaque",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoggedIn) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionIcon(
                        imageVector = if (isToWatch) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        isActive = isToWatch,
                        isLoading = isLoading,
                        onClick = onToWatchClick
                    )
                    ActionIcon(
                        imageVector = if (isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        isActive = isWatched,
                        isLoading = isLoading,
                        onClick = onWatchedClick
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val duration = movie.runtime?.let {
                        val hours = it / 60
                        val minutes = it % 60
                        if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
                    } ?: ""

                    val genre = movie.genres?.firstOrNull()?.name ?: ""
                    val year = movie.releaseDate?.take(4) ?: ""

                    val metadata = listOfNotNull(
                        duration.takeIf { it.isNotEmpty() },
                        genre.takeIf { it.isNotEmpty() },
                        year.takeIf { it.isNotEmpty() }
                    ).joinToString(" • ")

                    Text(
                        text = metadata,
                        color = TextGrey,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    val rating = movie.rating ?: 0.0
                    val ratingText = if (rating > 0) String.format("%.1f", rating) else "—"

                    Text(
                        text = ratingText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (movie.voteCount != null && movie.voteCount > 0) {
                        Text(
                            text = " (${movie.voteCount})",
                            color = TextGrey,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = PremiumGold,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = if (isActive) PremiumGold else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MovieHorizontalSection(
    title: String,
    movies: List<MovieResponse>,
    watchlistStatusMap: Map<Long, String>,
    isLoggedIn: Boolean,
    onMovieClick: (Long) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { movies.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4.seconds)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % movies.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            pageSize = PageSize.Fixed(160.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val movie = movies[page]
            val status = if (isLoggedIn) watchlistStatusMap[movie.id] else null
            MoviePosterCard(
                movie = movie,
                libraryStatus = status,
                onClick = { onMovieClick(movie.id) }
            )
        }
    }
}

@Composable
fun MoviePosterCard(
    movie: MovieResponse,
    libraryStatus: String?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Rating Badge
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))

                    val rating = movie.rating ?: 0.0
                    val ratingText = if (rating > 0) String.format("%.1f", rating) else "—"

                    Text(
                        text = ratingText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (libraryStatus != null) {
                val icon = if (libraryStatus == "WATCHED") Icons.Default.CheckCircle else Icons.Default.Bookmark
                val bgColor = if (libraryStatus == "WATCHED") PremiumGold else AccentBlue
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(bgColor.copy(alpha = 0.8f))
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (libraryStatus == "WATCHED") Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val genre = movie.genres?.firstOrNull()?.name
        val duration = if (movie.runtime != null) "${movie.runtime}min" else null

        val metadata = listOfNotNull(duration, genre).joinToString(" • ")

        if (metadata.isNotEmpty()) {
            Text(
                text = metadata,
                color = TextGrey,
                fontSize = 12.sp
            )
        }
    }
}
