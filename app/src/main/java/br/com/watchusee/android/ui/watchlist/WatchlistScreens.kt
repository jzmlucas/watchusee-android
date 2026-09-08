package br.com.watchusee.android.ui.watchlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LibraryMovieCard
import br.com.watchusee.android.ui.components.MovieGridSkeleton
import br.com.watchusee.android.ui.components.MovieReelsActions
import br.com.watchusee.android.ui.components.WatchuSeeTopBar
import br.com.watchusee.android.ui.theme.DarkNavy
import br.com.watchusee.android.ui.theme.PremiumGold
import br.com.watchusee.android.ui.theme.SurfaceGrey
import br.com.watchusee.android.ui.theme.TextGrey
import br.com.watchusee.android.ui.theme.TextWhite
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.WatchlistSortOrder
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
    onHomeClick: () -> Unit = {},
    viewModel: WatchlistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onRequireLogin: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(initialTab)
    }

    var isSearchExpanded by remember {
        mutableStateOf(false)
    }

    var showSortMenu by remember {
        mutableStateOf(false)
    }

    val haptic = LocalHapticFeedback.current

    val tabs = listOf(
        "Para Assistir",
        "Assistidos"
    )

    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val toWatchTotalElements by viewModel.toWatchTotalElements.collectAsStateWithLifecycle()
    val watchedTotalElements by viewModel.watchedTotalElements.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (authViewModel.isAuthenticated()) {
            viewModel.loadToWatch()
            viewModel.loadWatched()
        } else {
            onRequireLogin()
        }
    }

    val toWatchCount = toWatchTotalElements.toInt()
    val watchedCount = watchedTotalElements.toInt()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .statusBarsPadding()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(43.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (isSearchExpanded) {

                        BasicTextField(
                            value = query,
                            onValueChange = viewModel::onQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .background(
                                    color = SurfaceGrey.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(17.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = PremiumGold.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(17.dp)
                                ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(PremiumGold),
                            decorationBox = { innerTextField ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(
                                        modifier = Modifier.width(8.dp)
                                    )

                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        if (query.isEmpty()) {
                                            Text(
                                                text = "Buscar filme na biblioteca...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextGrey,
                                                fontSize = 14.sp
                                            )
                                        }

                                        innerTextField()
                                    }

                                    if (query.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                viewModel.onQueryChange("")
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Limpar",
                                                tint = TextGrey,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            isSearchExpanded = false
                                            viewModel.onQueryChange("")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Fechar",
                                            tint = TextWhite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                    } else {

                        Text(
                            text = "Biblioteca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.clickable {
                                onHomeClick()
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            IconButton(
                                onClick = {
                                    isSearchExpanded = true
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Buscar",
                                    tint = TextWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Box {

                                IconButton(
                                    onClick = {
                                        showSortMenu = true
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Sort,
                                        contentDescription = "Ordenar",
                                        tint = if (
                                            sortOrder != WatchlistSortOrder.RECENT
                                        ) {
                                            PremiumGold
                                        } else {
                                            TextWhite
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = {
                                        showSortMenu = false
                                    },
                                    modifier = Modifier
                                        .background(SurfaceGrey)
                                        .border(
                                            width = 1.dp,
                                            color = PremiumGold.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clip(
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {

                                    WatchlistSortOrder.entries.forEach { order ->

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = order.displayName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (
                                                        sortOrder == order
                                                    ) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    },
                                                    color = if (
                                                        sortOrder == order
                                                    ) {
                                                        PremiumGold
                                                    } else {
                                                        TextWhite
                                                    }
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSortOrder(order)
                                                showSortMenu = false
                                            },
                                            leadingIcon = {
                                                if (sortOrder == order) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = PremiumGold,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 2.dp,
                            bottom = 4.dp
                        )
                        .background(
                            color = SurfaceGrey.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    tabs.forEachIndexed { index, title ->

                        val isSelected = selectedTab == index
                        val count = if (index == 0) {
                            toWatchCount
                        } else {
                            watchedCount
                        }

                        val backgroundColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                PremiumGold
                            } else {
                                Color.Transparent
                            },
                            animationSpec = tween(300),
                            label = "tabBackground"
                        )

                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                DarkNavy
                            } else {
                                TextGrey
                            },
                            animationSpec = tween(300),
                            label = "tabText"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(backgroundColor)
                                .clickable {

                                    if (selectedTab != index) {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.LongPress
                                        )

                                        selectedTab = index
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) {
                                        FontWeight.ExtraBold
                                    } else {
                                        FontWeight.SemiBold
                                    },
                                    color = textColor,
                                    fontSize = 13.sp
                                )

                                Spacer(
                                    modifier = Modifier.width(6.dp)
                                )

                                Surface(
                                    color = if (isSelected) {
                                        DarkNavy.copy(alpha = 0.2f)
                                    } else {
                                        SurfaceGrey.copy(alpha = 0.5f)
                                    },
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = count.toString(),
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkNavy
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
                    viewModel = viewModel
                )

                1 -> WatchedTab(
                    onMovieClick = onMovieClick,
                    onNavigateToSearch = onNavigateToSearch,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ToWatchScreen(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit = {}
) {
    LibraryScreen(
        onMovieClick = onMovieClick,
        onNavigateToSearch = onNavigateToSearch,
        viewModel = viewModel,
        authViewModel = authViewModel,
        onRequireLogin = onRequireLogin,
        initialTab = 0
    )
}

@Composable
fun WatchedScreen(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onRequireLogin: () -> Unit,
    onLogout: () -> Unit = {}
) {
    LibraryScreen(
        onMovieClick = onMovieClick,
        onNavigateToSearch = onNavigateToSearch,
        viewModel = viewModel,
        authViewModel = authViewModel,
        onRequireLogin = onRequireLogin,
        initialTab = 1
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToWatchTab(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel
) {
    val uiState by viewModel.toWatchState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    var showEmptyAnimation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->

            if (lastVisibleIndex == null) {
                return@collect
            }

            val totalItems = when (val state = uiState) {
                is WatchlistUiState.Success -> state.items.size
                else -> 0
            }

            if (
                viewModel.shouldLoadMoreToWatch(
                    lastVisibleIndex = lastVisibleIndex,
                    totalItems = totalItems
                )
            ) {
                viewModel.loadMoreToWatch()
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is WatchlistUiState.Empty) {
            showEmptyAnimation = true
            delay(500)
            showEmptyAnimation = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )

                viewModel.loadToWatch(
                    isRefresh = true
                )
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
                        val result = snackbarHostState.showSnackbar(
                            message = "Removido da lista",
                            actionLabel = "Desfazer",
                            duration = SnackbarDuration.Short
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoLastRemoval()
                        }
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
                modifier = Modifier.fillMaxSize(),
                onRetry = viewModel::loadToWatch,
                showEmptyAnimation = showEmptyAnimation,
                onEmptyAction = onNavigateToSearch,
                isWatchedTab = false
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchedTab(
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel
) {
    val uiState by viewModel.watchedState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->

            if (lastVisibleIndex == null) {
                return@collect
            }

            val totalItems = when (val state = uiState) {
                is WatchlistUiState.Success -> state.items.size
                else -> 0
            }

            if (
                viewModel.shouldLoadMoreWatched(
                    lastVisibleIndex = lastVisibleIndex,
                    totalItems = totalItems
                )
            ) {
                viewModel.loadMoreWatched()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )

                viewModel.loadWatched(
                    isRefresh = true
                )
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
                        val result = snackbarHostState.showSnackbar(
                            message = "Removido dos assistidos",
                            actionLabel = "Desfazer",
                            duration = SnackbarDuration.Short
                        )

                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoLastRemoval()
                        }
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
                modifier = Modifier.fillMaxSize(),
                onRetry = viewModel::loadWatched,
                onEmptyAction = onNavigateToSearch,
                isWatchedTab = true
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
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
    modifier: Modifier = Modifier,
    onAction: (Long) -> Unit,
    showEmptyAnimation: Boolean = false,
    onEmptyAction: () -> Unit,
    isWatchedTab: Boolean = false
) {
    when (uiState) {
        WatchlistUiState.Loading -> {
            MovieGridSkeleton(
                modifier = modifier,
                columns = 2
            )
        }

        is WatchlistUiState.Empty -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 32.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = showEmptyAnimation,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(500)
                        ) togetherWith fadeOut(
                            animationSpec = tween(500)
                        )
                    },
                    label = "WatchlistEmptyAnimation"
                ) {
                    EmptyState(
                        message = if (isWatchedTab) {
                            "Você ainda não marcou nenhum filme como assistido"
                        } else {
                            "Você ainda não adicionou filmes para assistir"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        actionLabel = "Descobrir Filmes",
                        onAction = onEmptyAction
                    )
                }
            }
        }

        is WatchlistUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorState(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is WatchlistUiState.Success -> {
            val uniqueItems = remember(uiState.items) {
                uiState.items.distinctBy { it.movie.id }
            }

            if (uniqueItems.isEmpty()) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 32.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = if (isWatchedTab) {
                            "Você ainda não marcou nenhum filme como assistido"
                        } else {
                            "Você ainda não adicionou filmes para assistir"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        actionLabel = "Descobrir Filmes",
                        onAction = onEmptyAction
                    )
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 120.dp
                    ),
                    modifier = modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uniqueItems,
                        key = { item ->
                            item.movie.id
                        }
                    ) { item ->

                        val movie = item.movie

                        LibraryMovieCard(
                            movie = movie,
                            isWatched = isWatchedTab,
                            onClick = {
                                onMovieClick(movie.id)
                            },
                            onRemoveClick = {
                                onRemove(movie.id)
                            },
                            onToggleWatchedClick = {
                                onAction(movie.id)
                            },
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(500),
                                placementSpec = tween(500)
                            )
                        )
                    }
                }
            }
        }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .scaleOnClick(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            AsyncImage(
                model = TmdbImageUrl.getBackdropUrl(
                    movie.backdropPath
                ) ?: TmdbImageUrl.getPosterUrl(
                    movie.posterPath,
                    "w780"
                ),
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
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0.5f
                        )
                    )
            )

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
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                movie.releaseDate
                    ?.take(4)
                    ?.let { year ->
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
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
