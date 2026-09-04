package br.com.watchusee.android.ui.shares

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.ShareResponse
import br.com.watchusee.android.data.dto.ShareStatus
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.ui.components.MoviePosterCard
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.ShareActionState
import br.com.watchusee.android.viewmodel.ShareUiState
import br.com.watchusee.android.viewmodel.ShareViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharesScreen(
    onMovieClick: (Long) -> Unit,
    onRequireLogin: (() -> Unit) -> Unit,
    viewModel: ShareViewModel = hiltViewModel(),
    authViewModel: br.com.watchusee.android.viewmodel.AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Recebidos", "Enviados")

    val receivedState by viewModel.receivedState.collectAsStateWithLifecycle()
    val sentState by viewModel.sentState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val movieDetails by viewModel.movieDetails.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadReceivedShares()
            viewModel.loadSentShares()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin {}
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is ShareActionState.Success) {
            scope.launch {
                snackbarHostState.showSnackbar("Ação realizada com sucesso!")
            }
            viewModel.resetActionState()
        } else if (actionState is ShareActionState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((actionState as ShareActionState.Error).message)
            }
            viewModel.resetActionState()
        }
    }

    if (currentUser == null) return

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        "COMPARTILHAMENTOS",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.loadReceivedShares(isRefresh = true)
                    viewModel.loadSentShares(isRefresh = true)
                },
                modifier = Modifier.weight(1f)
            ) {
                when (selectedTab) {
                    0 -> ShareList(
                        state = receivedState,
                        movieDetails = movieDetails,
                        isReceived = true,
                        onAccept = { viewModel.acceptShare(it) },
                        onReject = { viewModel.rejectShare(it) },
                        onMovieClick = onMovieClick,
                        onRetry = { viewModel.loadReceivedShares() }
                    )
                    1 -> ShareList(
                        state = sentState,
                        movieDetails = movieDetails,
                        isReceived = false,
                        onMovieClick = onMovieClick,
                        onRetry = { viewModel.loadSentShares() }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareList(
    state: ShareUiState,
    movieDetails: Map<Long, MovieResponse>,
    isReceived: Boolean,
    onAccept: (Long) -> Unit = {},
    onReject: (Long) -> Unit = {},
    onMovieClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is ShareUiState.Loading -> LoadingState()
        is ShareUiState.Error -> ErrorState(message = state.message, onRetry = onRetry)
        is ShareUiState.Success -> {
            if (state.shares.isEmpty()) {
                EmptyState(
                    message = if (isReceived) "Nenhum compartilhamento recebido." else "Você ainda não compartilhou filmes.",
                    icon = if (isReceived) Icons.Default.MailOutline else Icons.AutoMirrored.Filled.Send
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(state.shares, key = { it.id }) { share ->
                        ShareListItem(
                            share = share,
                            movie = movieDetails[share.movieId],
                            isReceived = isReceived,
                            onAccept = { onAccept(share.id) },
                            onReject = { onReject(share.id) },
                            onMovieClick = { onMovieClick(share.movieId) }
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun ShareListItem(
    share: ShareResponse,
    movie: MovieResponse?,
    isReceived: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMovieClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isReceived) Icons.Default.Person else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isReceived) "De: ${share.senderNick}" else "Para: ${share.recipientNick}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status = share.status)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = share.createdAt.take(10).split("-").reversed().joinToString("/"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Drag and Drop States
        var dragOffsetY by remember { mutableFloatStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }
        val animatedDragOffset by animateFloatAsState(
            targetValue = dragOffsetY,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "drag_offset"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isDragging = true
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetY += dragAmount.y
                        },
                        onDragEnd = {
                            if (dragOffsetY < -100f) {
                                if (isReceived && share.status == ShareStatus.PENDING) onAccept()
                            } else if (dragOffsetY > 100f) {
                                onReject()
                            }
                            dragOffsetY = 0f
                            isDragging = false
                        },
                        onDragCancel = {
                            dragOffsetY = 0f
                            isDragging = false
                        }
                    )
                }
                .graphicsLayer {
                    translationY = animatedDragOffset
                    scaleX = if (isDragging) 1.03f else 1f
                    scaleY = if (isDragging) 1.03f else 1f
                    alpha = if (isDragging) 0.85f else 1f
                    shadowElevation = if (isDragging) 15f else 0f
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onMovieClick() }
            ) {
                if (movie != null) {
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
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 0.2f
                                )
                            )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = movie?.title ?: "Carregando...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (!share.message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp),
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Text(
                    text = "\"${share.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: ShareStatus) {
    val color = when (status) {
        ShareStatus.PENDING -> Color(0xFFFFA000)
        ShareStatus.ACCEPTED -> Color(0xFF2E7D32)
        ShareStatus.REJECTED -> MaterialTheme.colorScheme.error
    }
    
    val text = when (status) {
        ShareStatus.PENDING -> "Pendente"
        ShareStatus.ACCEPTED -> "Aceito"
        ShareStatus.REJECTED -> "Recusado"
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}
