package br.com.watchusee.android.ui.watchlist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistItemResponse
import br.com.watchusee.android.ui.components.*
import br.com.watchusee.android.ui.components.MovieGridSkeleton
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.WatchlistUiState
import br.com.watchusee.android.viewmodel.WatchlistViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onRequireLogin: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    val tabs = listOf("Para Assistir", "Assistidos")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
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
                        "BIBLIOTECA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {},
                    modifier = Modifier.height(48.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
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
            when (selectedTab) {
                0 -> ToWatchTab(
                    onMovieClick = onMovieClick,
                    onNavigateToSearch = onNavigateToSearch,
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    onRequireLogin = onRequireLogin
                )
                1 -> WatchedTab(
                    onMovieClick = onMovieClick,
                    onNavigateToSearch = onNavigateToSearch,
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    onRequireLogin = onRequireLogin
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToWatchTab(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel,
    authViewModel: AuthViewModel,
    onRequireLogin: () -> Unit
) {
    val uiState by viewModel.toWatchState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var showEmptyAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadToWatch()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is WatchlistUiState.Empty) {
            showEmptyAnimation = true
            delay(500)
            showEmptyAnimation = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.loadToWatch(isRefresh = true)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            WatchlistContent(
                uiState = uiState,
                gridState = gridState,
                onMovieClick = onMovieClick,
                onRemove = { movieId ->
                    viewModel.removeFromToWatch(movieId)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Filme removido da lista",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onAction = { movieId ->
                    viewModel.markAsWatched(movieId)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Marcado como assistido!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                actionIcon = Icons.Default.Visibility,
                actionLabel = "Assistir",
                emptyMessage = "Sua lista de desejos está vazia",
                modifier = Modifier.fillMaxSize(),
                onRetry = { viewModel.loadToWatch() },
                showEmptyAnimation = showEmptyAnimation,
                onEmptyAction = onNavigateToSearch
            )
        }

        WatchlistSearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChange(it) },
            placeholder = "Buscar",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchedTab(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel,
    authViewModel: AuthViewModel,
    onRequireLogin: () -> Unit
) {
    val uiState by viewModel.watchedState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadWatched()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.loadWatched(isRefresh = true)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            WatchlistContent(
                uiState = uiState,
                gridState = gridState,
                onMovieClick = onMovieClick,
                onRemove = { movieId ->
                    viewModel.removeFromWatched(movieId)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Filme removido dos assistidos",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                onAction = { movieId ->
                    viewModel.addToWatch(movieId)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Movido para a lista!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                actionIcon = Icons.Default.Bookmark,
                actionLabel = "Lista",
                emptyMessage = "Você ainda não marcou nenhum filme como assistido",
                modifier = Modifier.fillMaxSize(),
                onRetry = { viewModel.loadWatched() },
                onEmptyAction = onNavigateToSearch,
                isWatchedTab = true
            )
        }

        WatchlistSearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChange(it) },
            placeholder = "Buscar",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ToWatchScreen(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel,
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel,
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.toWatchState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var showEmptyAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadToWatch()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is WatchlistUiState.Empty) {
            showEmptyAnimation = true
            delay(500)
            showEmptyAnimation = false
        }
    }

    if (currentUser == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "PARA ASSISTIR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.loadToWatch(isRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                WatchlistContent(
                    uiState = uiState,
                    gridState = gridState,
                    onMovieClick = onMovieClick,
                    onRemove = { movieId ->
                        viewModel.removeFromToWatch(movieId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Filme removido da lista",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onAction = { movieId ->
                        viewModel.markAsWatched(movieId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Marcado como assistido!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    actionIcon = Icons.Default.Visibility,
                    actionLabel = "Assistir",
                    emptyMessage = "Sua lista de desejos está vazia",
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { viewModel.loadToWatch() },
                    showEmptyAnimation = showEmptyAnimation,
                    onEmptyAction = onNavigateToSearch
                )
            }

            WatchlistSearchBar(
                query = query,
                onQueryChange = { viewModel.onQueryChange(it) },
                placeholder = "Buscar",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedScreen(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel,
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel,
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.watchedState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadWatched()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin()
        }
    }

    if (currentUser == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "ASSISTIDOS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.loadWatched(isRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                WatchlistContent(
                    uiState = uiState,
                    gridState = gridState,
                    onMovieClick = onMovieClick,
                    onRemove = { movieId ->
                        viewModel.removeFromWatched(movieId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Filme removido dos assistidos",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onAction = { movieId ->
                        viewModel.addToWatch(movieId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Movido para a lista!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    actionIcon = Icons.Default.Bookmark,
                    actionLabel = "Lista",
                    emptyMessage = "Você ainda não marcou nenhum filme como assistido",
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { viewModel.loadWatched() },
                    onEmptyAction = onNavigateToSearch,
                    isWatchedTab = true
                )
            }

            WatchlistSearchBar(
                query = query,
                onQueryChange = { viewModel.onQueryChange(it) },
                placeholder = "Buscar",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
            )
        }
    }
}

@Composable
fun WatchlistSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun WatchlistContent(
    uiState: WatchlistUiState,
    gridState: LazyGridState,
    onMovieClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onRetry: () -> Unit,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    onAction: ((Long) -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionLabel: String? = null,
    showEmptyAnimation: Boolean = false,
    onEmptyAction: (() -> Unit)? = null,
    isWatchedTab: Boolean = false
) {
    when (uiState) {
        is WatchlistUiState.Loading -> MovieGridSkeleton(modifier, columns = 3)
        is WatchlistUiState.Empty -> {
            AnimatedContent(
                targetState = showEmptyAnimation,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                },
                label = "WatchlistEmptyAnimation"
            ) { _ ->
                EmptyState(
                    message = emptyMessage,
                    modifier = modifier,
                    actionLabel = "Descobrir Filmes",
                    onAction = onEmptyAction
                )
            }
        }
        is WatchlistUiState.Error -> ErrorState(uiState.message, onRetry, modifier)
        is WatchlistUiState.Success -> {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 120.dp
                ),
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = uiState.items,
                    key = { _, item -> item.movie.id },
                    span = { index, _ ->
                        if (index == 0) GridItemSpan(3) else GridItemSpan(1)
                    }
                ) { index, item ->
                    val movie = item.movie
                    if (index == 0) {
                        LargeMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            onWatchedClick = if (!isWatchedTab) { { onAction?.invoke(movie.id) } } else null,
                            onToWatchClick = if (isWatchedTab) { { onAction?.invoke(movie.id) } } else null,
                            onDeleteClick = { onRemove(movie.id) },
                            isToWatch = !isWatchedTab,
                            isWatched = isWatchedTab
                        )
                    } else {
                        MoviePosterCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            index = index,
                            isToWatch = !isWatchedTab,
                            isWatched = isWatchedTab,
                            onDeleteClick = { onRemove(movie.id) },
                            onWatchedClick = if (!isWatchedTab) { { onAction?.invoke(movie.id) } } else null,
                            onToWatchClick = if (isWatchedTab) { { onAction?.invoke(movie.id) } } else null,
                            onSwipeLeft = { onRemove(movie.id) },
                            onSwipeRight = { onAction?.invoke(movie.id) },
                            swipeLeftIcon = Icons.Default.Delete,
                            swipeRightIcon = actionIcon ?: Icons.Default.CheckCircle,
                            swipeLeftColor = MaterialTheme.colorScheme.error,
                            swipeRightColor = if (isWatchedTab) MaterialTheme.colorScheme.primary else Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistMovieCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionLabel: String? = null,
    index: Int = 0,
    isLarge: Boolean = false
) {
    val toWatch = actionLabel == "Assistir"
    val watched = actionLabel == "Lista"

    if (isLarge) {
        LargeMovieCard(
            movie = movie,
            onClick = onClick,
            onWatchedClick = if (toWatch) onAction else null,
            onToWatchClick = if (watched) onAction else null,
            onDeleteClick = onRemove,
            isToWatch = toWatch,
            isWatched = watched
        )
    } else {
        MoviePosterCard(
            movie = movie,
            onClick = onClick,
            index = index,
            onWatchedClick = if (toWatch) onAction else null,
            onToWatchClick = if (watched) onAction else null,
            onDeleteClick = onRemove,
            isToWatch = toWatch,
            isWatched = watched
        )
    }
}

@Composable
private fun LargeMovieCard(
    movie: MovieResponse,
    onClick: () -> Unit,
    onToWatchClick: (() -> Unit)? = null,
    onWatchedClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    isToWatch: Boolean = false,
    isWatched: Boolean = false
) {
    var feedbackIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var feedbackColor by remember { mutableStateOf(Color.White) }
    val feedbackAlpha = remember { Animatable(0f) }
    val feedbackScale = remember { Animatable(0.5f) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val scope = rememberCoroutineScope()

    var prevToWatch by remember { mutableStateOf(isToWatch) }
    var prevWatched by remember { mutableStateOf(isWatched) }

    LaunchedEffect(isToWatch, isWatched) {
        val isRemoved = (prevToWatch && !isToWatch) || (prevWatched && !isWatched)
        val icon = when {
            isWatched -> Icons.Default.Visibility
            isToWatch -> Icons.Default.Bookmark
            isRemoved -> Icons.Default.Delete
            else -> null
        }

        if (icon != null) {
            feedbackIcon = icon
            feedbackColor = when {
                isWatched -> Color(0xFF2E7D32)
                isToWatch -> primaryColor
                else -> errorColor
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = TmdbImageUrl.getBackdropUrl(movie.backdropPath)
                    ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 0.4f
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
                            .size(80.dp)
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
                    .padding(16.dp)
                    .padding(end = 64.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = movie.releaseDate?.take(4) ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            MovieReelsActions(
                modifier = Modifier.align(Alignment.BottomEnd),
                onToWatchClick = onToWatchClick,
                onWatchedClick = onWatchedClick,
                onDeleteClick = onDeleteClick,
                isToWatch = isToWatch,
                isWatched = isWatched,
                isLarge = true
            )
        }
    }
}
