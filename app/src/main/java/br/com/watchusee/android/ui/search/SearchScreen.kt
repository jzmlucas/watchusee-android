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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.MovieGridSkeleton
import br.com.watchusee.android.ui.components.MoviePosterCard
import br.com.watchusee.android.viewmodel.SearchUiState
import br.com.watchusee.android.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (Long) -> Unit,
    onRequireLogin: (() -> Unit) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

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
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        TextField(
            value = query,
            onValueChange = {
                viewModel.onQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = {
                Text(
                    "Pesquisar filmes...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.onQueryChange("")
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.onQueryChange(query)
                }
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState,
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
                    is SearchUiState.Idle -> {
                        if (state.trendingMovies.isEmpty()) {
                            EmptyState(
                                message = "Descubra seu próximo filme favorito",
                                icon = Icons.Default.Movie
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Buscas Populares",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        top = 0.dp,
                                        end = 16.dp,
                                        bottom = 120.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(
                                        items = state.trendingMovies,
                                        key = { _, movie -> movie.id }
                                    ) { index, movie ->
                                        MoviePosterCard(
                                            movie = movie,
                                            onClick = { onMovieClick(movie.id) },
                                            index = index
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is SearchUiState.Loading -> {
                        MovieGridSkeleton(columns = 2)
                    }
                    is SearchUiState.Success -> {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = 120.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                            viewModel.toggleToWatch(movie.id)
                                        } else {
                                            onRequireLogin { viewModel.toggleToWatch(movie.id) }
                                        }
                                    },
                                    onWatchedClick = {
                                        if (authViewModel.isAuthenticated()) {
                                            viewModel.toggleWatched(movie.id)
                                        } else {
                                            onRequireLogin { viewModel.toggleWatched(movie.id) }
                                        }
                                    },
                                    onSwipeLeft = {
                                        if (authViewModel.isAuthenticated()) {
                                            viewModel.toggleToWatch(movie.id)
                                        } else {
                                            onRequireLogin { viewModel.toggleToWatch(movie.id) }
                                        }
                                    },
                                    onSwipeRight = {
                                        if (authViewModel.isAuthenticated()) {
                                            viewModel.toggleWatched(movie.id)
                                        } else {
                                            onRequireLogin { viewModel.toggleWatched(movie.id) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    is SearchUiState.Empty -> {
                        EmptyState(
                            message = "Nenhum resultado para \"$query\"",
                            icon = Icons.Default.SearchOff
                        )
                    }
                    is SearchUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.onQueryChange(query) }
                        )
                    }
                }
            }
        }
    }
}