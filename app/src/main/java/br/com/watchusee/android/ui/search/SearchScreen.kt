package br.com.watchusee.android.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistStatusResponse
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.SearchUiState
import br.com.watchusee.android.viewmodel.SearchViewModel
import coil3.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.ui.components.MovieGridSkeleton



@Composable
fun SearchScreen(
    onMovieClick: (Long) -> Unit,
    onBack: () -> Unit,
    onRequireLogin: (() -> Unit) -> Unit = {}
) {
    val searchViewModel: SearchViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()

    val authViewModel: AuthViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()

    val query by searchViewModel.query.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(18.dp),
            color = SurfaceGrey
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Voltar",
                        tint = PremiumGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = query,
                    onValueChange = { newQuery ->
                        searchViewModel.onQueryChange(newQuery)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                        }
                    ),
                    decorationBox = { innerTextField ->

                        if (query.isEmpty()) {
                            Text(
                                text = "Procurar por filmes...",
                                color = TextGrey,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        innerTextField()
                    }
                )

                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            searchViewModel.onQueryChange("")
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Limpar busca",
                            tint = TextGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        SearchContent(
            searchViewModel = searchViewModel,
            authViewModel = authViewModel,
            onMovieClick = onMovieClick,
            onRequireLogin = onRequireLogin,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SearchContent(
    searchViewModel: SearchViewModel,
    authViewModel: AuthViewModel,
    onMovieClick: (Long) -> Unit,
    onRequireLogin: (() -> Unit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val query by searchViewModel.query.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = searchUiState,
            transitionSpec = {
                if (initialState is SearchUiState.Success && targetState is SearchUiState.Success) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            },
            contentKey = { state ->
                when (state) {
                    is SearchUiState.Success -> "success"
                    else -> state.javaClass.simpleName
                }
            },
            label = "search_content"
        ) { state ->
            when (state) {
                is SearchUiState.Loading -> {
                    SearchSkeleton()
                }
                is SearchUiState.Success -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 100.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = state.movies,
                            key = { _, movie -> movie.id }
                        ) { index, movie ->
                            val status = state.statuses[movie.id]
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                index = index,
                                isToWatch = status?.toWatch == true,
                                isWatched = status?.watched == true,
                                onToWatchClick = {
                                    if (authViewModel.isAuthenticated()) {
                                        searchViewModel.toggleToWatch(movie.id)
                                    } else {
                                        onRequireLogin { searchViewModel.toggleToWatch(movie.id) }
                                    }
                                },
                                onWatchedClick = {
                                    if (authViewModel.isAuthenticated()) {
                                        searchViewModel.toggleWatched(movie.id)
                                    } else {
                                        onRequireLogin { searchViewModel.toggleWatched(movie.id) }
                                    }
                                }
                            )
                        }
                    }
                }
                is SearchUiState.Empty -> {
                    EmptyState(
                        message = "Nenhum resultado para \"$query\"",
                        icon = Icons.Rounded.SearchOff
                    )
                }
                is SearchUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { searchViewModel.onQueryChange(query) }
                    )
                }
                is SearchUiState.Idle -> {
                    if (query.isEmpty()) {
                        if (state.trendingMovies.isEmpty()) {
                            SearchSkeleton()
                        } else {
                            TrendingMoviesGrid(
                                movies = state.trendingMovies,
                                onMovieClick = onMovieClick,
                                statuses = state.statuses
                            )
                        }
                    } else {
                        SearchSkeleton()
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendingMoviesGrid(
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    statuses: Map<Long, WatchlistStatusResponse> = emptyMap()
) {
    if (movies.isEmpty()) {
        EmptyState(
            message = "Digite algo para buscar",
            icon = Icons.Rounded.Search
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Buscas populares",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 0.dp,
                    end = 16.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = movies,
                    key = { _, movie -> movie.id }
                ) { index, movie ->
                    TrendingMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) },
                        index = index,
                        isToWatch = statuses[movie.id]?.toWatch == true,
                        isWatched = statuses[movie.id]?.watched == true
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingMovieCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    index: Int,
    isToWatch: Boolean = false,
    isWatched: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .scaleOnClick(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                            startY = 0.7f
                        )
                    )
            )

            if (index < 3) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            if (index == 0) Color(0xFFFFD700)
                            else if (index == 1) Color(0xFFC0C0C0)
                            else Color(0xFFCD7F32),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            if (isToWatch || isWatched) {
                LibraryStatusBanner(
                    isToWatch = isToWatch,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun LibraryStatusBanner(
    isToWatch: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isToWatch) Color(0xFF4CAF50).copy(alpha = 0.9f)
                else Color(0xFF2196F3).copy(alpha = 0.9f)
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isToWatch) Icons.Rounded.Bookmark else Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isToWatch) "NA LISTA" else "VISTO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun MoviePosterCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    index: Int,
    isToWatch: Boolean = false,
    isWatched: Boolean = false,
    onToWatchClick: () -> Unit = {},
    onWatchedClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .scaleOnClick(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                            startY = 0.7f
                        )
                    )
            )

            if (isToWatch || isWatched) {
                LibraryStatusBanner(
                    isToWatch = isToWatch,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun SearchSkeleton(
    modifier: Modifier = Modifier
) {
    MovieGridSkeleton(
        modifier = modifier,
        columns = 2
    )
}