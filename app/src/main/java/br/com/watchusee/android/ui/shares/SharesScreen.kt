package br.com.watchusee.android.ui.shares

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.ShareResponse
import br.com.watchusee.android.data.dto.ShareStatus
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
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
    onHomeClick: () -> Unit = {},
    viewModel: ShareViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val receivedState by viewModel.receivedState
        .collectAsStateWithLifecycle()

    val sentState by viewModel.sentState
        .collectAsStateWithLifecycle()

    val isRefreshing by viewModel.isRefreshing
        .collectAsStateWithLifecycle()

    val currentUser by authViewModel.currentUser
        .collectAsStateWithLifecycle()

    val actionState by viewModel.actionState
        .collectAsStateWithLifecycle()

    val movieDetails by viewModel.movieDetails
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    val scope = rememberCoroutineScope()

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(currentUser) {

        if (currentUser != null) {

            viewModel.loadReceivedShares()
            viewModel.loadSentShares()

        } else if (!authViewModel.isAuthenticated()) {

            onRequireLogin {}

        }
    }


    LaunchedEffect(actionState) {

        when (actionState) {

            is ShareActionState.Success -> {

                haptic.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Compartilhamento atualizado"
                    )
                }

                viewModel.resetActionState()
            }

            is ShareActionState.Error -> {

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (
                                actionState as ShareActionState.Error
                                ).message
                    )
                }

                viewModel.resetActionState()
            }

            else -> Unit
        }
    }

    if (currentUser == null) {
        return
    }

    val receivedCount =
        (receivedState as? ShareUiState.Success)
            ?.shares
            ?.size
            ?: 0

    val sentCount =
        (sentState as? ShareUiState.Success)
            ?.shares
            ?.size
            ?: 0


    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            SharesHeader(
                selectedTab = selectedTab,
                receivedCount = receivedCount,
                sentCount = sentCount,
                onTabSelected = {
                    selectedTab = it
                }
            )

            PullToRefreshBox(

                isRefreshing = isRefreshing,

                onRefresh = {

                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )

                    viewModel.loadReceivedShares(
                        isRefresh = true
                    )

                    viewModel.loadSentShares(
                        isRefresh = true
                    )
                },

                modifier = Modifier.fillMaxSize()

            ) {

                AnimatedContent(
                    targetState = selectedTab,
                    label = "shares_tab_animation"
                ) { tab ->

                    when (tab) {

                        0 -> {

                            ShareList(
                                state = receivedState,
                                movieDetails = movieDetails,
                                isReceived = true,

                                onAccept = {
                                    viewModel.acceptShare(it)
                                },

                                onReject = {
                                    viewModel.rejectShare(it)
                                },

                                onMovieClick = onMovieClick,

                                onRetry = {
                                    viewModel.loadReceivedShares()
                                }
                            )
                        }


                        1 -> {

                            ShareList(
                                state = sentState,
                                movieDetails = movieDetails,
                                isReceived = false,

                                onMovieClick = onMovieClick,

                                onRetry = {
                                    viewModel.loadSentShares()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharesHeader(
    selectedTab: Int,
    receivedCount: Int,
    sentCount: Int,
    onTabSelected: (Int) -> Unit
) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background
            )
            .statusBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp
            )

    ) {

        Text(
            text = "Compartilhamentos",

            style = MaterialTheme.typography.headlineSmall,

            fontWeight = FontWeight.Bold,

            color = MaterialTheme.colorScheme.onBackground
        )


        Spacer(
            modifier = Modifier.height(4.dp)
        )


        Text(
            text = "Descubra filmes que seus amigos recomendaram",

            style = MaterialTheme.typography.bodyMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant,

            maxLines = 1,

            overflow = TextOverflow.Ellipsis
        )


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        SharesTabs(
            selectedTab = selectedTab,
            receivedCount = receivedCount,
            sentCount = sentCount,
            onTabSelected = onTabSelected
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}


@Composable
private fun SharesTabs(
    selectedTab: Int,
    receivedCount: Int,
    sentCount: Int,
    onTabSelected: (Int) -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                MaterialTheme.colorScheme.surfaceVariant
                    .copy(alpha = 0.45f)
            )
            .padding(4.dp)

    ) {

        ShareTab(
            title = "Recebidos",
            count = receivedCount,
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
            },
            modifier = Modifier.weight(1f)
        )


        ShareTab(
            title = "Enviados",
            count = sentCount,
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ShareTab(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(

        modifier = modifier
            .height(42.dp)
            .clickable(
                onClick = onClick
            ),

        shape = RoundedCornerShape(11.dp),

        color = if (selected) {
            MaterialTheme.colorScheme.surface
        } else {
            Color.Transparent
        },

        shadowElevation = if (selected) {
            2.dp
        } else {
            0.dp
        }

    ) {

        Row(

            modifier = Modifier.fillMaxSize(),

            horizontalArrangement = Arrangement.Center,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Text(
                text = title,

                style = MaterialTheme.typography.labelLarge,

                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },

                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )


            if (count > 0) {

                Spacer(
                    modifier = Modifier.width(6.dp)
                )


                Surface(

                    shape = CircleShape,

                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                            .copy(alpha = 0.15f)
                    }

                ) {

                    Text(

                        text = count.toString(),

                        modifier = Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 2.dp
                        ),

                        style = MaterialTheme.typography.labelSmall,

                        fontWeight = FontWeight.Bold,

                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ShareList(
    state: ShareUiState,
    movieDetails: Map<Long, MovieResponse>,
    isReceived: Boolean,
    onAccept: (Long) -> Unit = {},
    onReject: (Long) -> Unit = {},
    onMovieClick: (Long) -> Unit,
    onRetry: () -> Unit
) {

    when (state) {

        is ShareUiState.Loading -> {

            LoadingState()
        }

        is ShareUiState.Error -> {

            ErrorState(
                message = state.message,
                onRetry = onRetry
            )
        }

        is ShareUiState.Success -> {

            if (state.shares.isEmpty()) {

                EmptySharesState(
                    isReceived = isReceived
                )

            } else {

                LazyColumn(

                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 24.dp
                    ),

                    verticalArrangement = Arrangement.spacedBy(
                        14.dp
                    )

                ) {

                    items(
                        items = state.shares,
                        key = {
                            it.id
                        }
                    ) { share ->

                        ShareRecommendationCard(

                            share = share,

                            movie = movieDetails[
                                share.movieId
                            ],

                            isReceived = isReceived,

                            onAccept = {
                                onAccept(share.id)
                            },

                            onReject = {
                                onReject(share.id)
                            },

                            onMovieClick = {
                                onMovieClick(
                                    share.movieId
                                )
                            }
                        )
                    }
                }
            }
        }

        else -> Unit
    }
}

@Composable
private fun ShareRecommendationCard(
    share: ShareResponse,
    movie: MovieResponse?,
    isReceived: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMovieClick: () -> Unit
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
                .copy(alpha = 0.35f)
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )

    ) {

        Column {

            SharePersonHeader(
                share = share,
                isReceived = isReceived
            )


            if (movie != null) {

                MoviePreview(
                    movie = movie,
                    onClick = onMovieClick
                )

            } else {

                MovieLoadingPlaceholder()
            }


            if (!share.message.isNullOrBlank()) {

                ShareMessage(
                    message = share.message
                )
            }


            if (
                isReceived &&
                share.status == ShareStatus.PENDING
            ) {

                ShareActions(
                    onReject = onReject,
                    onAccept = onAccept
                )
            }
        }
    }
}

@Composable
private fun SharePersonHeader(
    share: ShareResponse,
    isReceived: Boolean
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(

            modifier = Modifier.size(42.dp),

            shape = CircleShape,

            color =
                MaterialTheme.colorScheme.primaryContainer

        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                Icon(

                    imageVector =
                        Icons.Rounded.Person,

                    contentDescription = null,

                    tint =
                        MaterialTheme.colorScheme.primary
                )
            }
        }


        Spacer(
            modifier = Modifier.width(11.dp)
        )


        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(

                text = if (isReceived) {
                    share.senderNick
                } else {
                    share.recipientNick
                },

                style =
                    MaterialTheme.typography.bodyLarge,

                fontWeight =
                    FontWeight.Bold,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Text(

                text = if (isReceived) {
                    "recomendou um filme para você"
                } else {
                    "recebeu sua recomendação"
                },

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier = Modifier.height(2.dp)
            )


            Text(

                text = formatDate(
                    share.createdAt
                ),

                style =
                    MaterialTheme.typography.labelSmall,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
                        .copy(alpha = 0.65f)
            )
        }


        Spacer(
            modifier = Modifier.width(8.dp)
        )


        StatusBadge(
            status = share.status
        )
    }
}

@Composable
private fun MoviePreview(
    movie: MovieResponse,
    onClick: () -> Unit
) {

    Box(

        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 12.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .clickable(
                onClick = onClick
            )

    ) {

        AsyncImage(

            model =
                TmdbImageUrl.getBackdropUrl(
                    movie.backdropPath
                ) ?: TmdbImageUrl.getPosterUrl(
                    movie.posterPath,
                    "w780"
                ),

            contentDescription =
                movie.title,

            modifier = Modifier.fillMaxSize(),

            contentScale =
                ContentScale.Crop
        )

        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(

                    Brush.verticalGradient(

                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.90f)
                        ),

                        startY = 70f
                    )
                )
        )


        Surface(

            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),

            shape = CircleShape,

            color = Color.Black.copy(
                alpha = 0.45f
            )

        ) {

            Icon(

                imageVector =
                    Icons.Rounded.Visibility,

                contentDescription =
                    "Ver detalhes",

                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp),

                tint = Color.White
            )
        }


        Column(

            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)

        ) {

            Text(

                text = movie.title,

                style =
                    MaterialTheme.typography.titleLarge,

                fontWeight =
                    FontWeight.Bold,

                color = Color.White,

                maxLines = 2,

                overflow =
                    TextOverflow.Ellipsis
            )


            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(

                    imageVector =
                        Icons.Rounded.Movie,

                    contentDescription = null,

                    modifier =
                        Modifier.size(14.dp),

                    tint =
                        Color.White.copy(
                            alpha = 0.80f
                        )
                )


                Spacer(
                    modifier = Modifier.width(5.dp)
                )


                Text(

                    text =
                        "Ver detalhes do filme",

                    style =
                        MaterialTheme.typography.labelMedium,

                    color =
                        Color.White.copy(
                            alpha = 0.85f
                        )
                )
            }
        }
    }
}


@Composable
private fun MovieLoadingPlaceholder() {

    Box(

        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(horizontal = 12.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                MaterialTheme.colorScheme
                    .surfaceVariant
                    .copy(alpha = 0.55f)
            ),

        contentAlignment = Alignment.Center

    ) {

        CircularProgressIndicator(

            modifier = Modifier.size(26.dp),

            strokeWidth = 2.dp
        )
    }
}


@Composable
private fun ShareMessage(
    message: String
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp
            )
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                MaterialTheme.colorScheme
                    .surfaceVariant
                    .copy(alpha = 0.45f)
            )
            .padding(12.dp),

        verticalAlignment =
            Alignment.Top

    ) {

        Text(

            text = "“",

            style =
                MaterialTheme.typography.headlineMedium,

            color =
                MaterialTheme.colorScheme.primary,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.width(5.dp)
        )


        Text(

            text = message,

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,

            maxLines = 3,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun ShareActions(
    onReject: () -> Unit,
    onAccept: () -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        OutlinedButton(

            onClick = onReject,

            modifier = Modifier
                .weight(1f)
                .height(48.dp),

            shape =
                RoundedCornerShape(14.dp)
        ) {

            Icon(

                imageVector =
                    Icons.Rounded.Close,

                contentDescription = null,

                modifier =
                    Modifier.size(18.dp)
            )


            Spacer(
                modifier = Modifier.width(6.dp)
            )


            Text(
                text = "Recusar",
                fontWeight =
                    FontWeight.SemiBold
            )
        }

        Button(

            onClick = onAccept,

            modifier = Modifier
                .weight(1f)
                .height(48.dp),

            shape =
                RoundedCornerShape(14.dp)
        ) {

            Icon(

                imageVector =
                    Icons.Rounded.Check,

                contentDescription = null,

                modifier =
                    Modifier.size(18.dp)
            )


            Spacer(
                modifier = Modifier.width(6.dp)
            )


            Text(

                text = "Aceitar",

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: ShareStatus
) {

    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val text: String
    val color: Color


    when (status) {

        ShareStatus.PENDING -> {

            icon = Icons.Rounded.Schedule
            text = "Pendente"
            color = Color(0xFFFFA000)
        }


        ShareStatus.ACCEPTED -> {

            icon = Icons.Rounded.Check
            text = "Aceito"
            color = Color(0xFF2E7D32)
        }


        ShareStatus.REJECTED -> {

            icon = Icons.Rounded.Close
            text = "Recusado"
            color =
                MaterialTheme.colorScheme.error
        }
    }


    Surface(

        shape = RoundedCornerShape(50),

        color =
            color.copy(alpha = 0.10f),

        border = BorderStroke(
            1.dp,
            color.copy(alpha = 0.18f)
        )
    ) {

        Row(

            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Icon(

                imageVector = icon,

                contentDescription = null,

                modifier = Modifier.size(13.dp),

                tint = color
            )


            Spacer(
                modifier = Modifier.width(4.dp)
            )


            Text(

                text = text,

                style =
                    MaterialTheme.typography.labelSmall,

                color = color,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptySharesState(
    isReceived: Boolean
) {

    Box(

        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),

        contentAlignment =
            Alignment.Center

    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(

                modifier =
                    Modifier.size(80.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme
                        .primaryContainer

            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector = if (
                            isReceived
                        ) {
                            Icons.Rounded.MailOutline
                        } else {
                            Icons.AutoMirrored.Rounded.Send
                        },

                        contentDescription = null,

                        modifier =
                            Modifier.size(34.dp),

                        tint =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            Text(

                text = if (isReceived) {
                    "Sua caixa está vazia"
                } else {
                    "Nenhuma recomendação ainda"
                },

                style =
                    MaterialTheme.typography.titleLarge,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Text(

                text = if (isReceived) {

                    "Quando um amigo compartilhar um filme\n" +
                            "com você, ele aparecerá aqui."

                } else {

                    "Compartilhe um filme com seus amigos\n" +
                            "e comece uma conversa."
                },

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}

private fun formatDate(
    dateString: String
): String {

    return try {

        dateString
            .take(10)
            .split("-")
            .reversed()
            .joinToString("/")

    } catch (e: Exception) {

        dateString
    }
}