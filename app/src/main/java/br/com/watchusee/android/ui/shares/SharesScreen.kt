package br.com.watchusee.android.ui.shares

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.ui.shares.components.ShareList
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ShareActionState
import br.com.watchusee.android.viewmodel.ShareUiState
import br.com.watchusee.android.viewmodel.ShareViewModel
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
    var selectedTab by remember { mutableIntStateOf(0) }

    val receivedState by viewModel.receivedState.collectAsStateWithLifecycle()
    val sentState by viewModel.sentState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val movieDetails by viewModel.movieDetails.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadReceivedShares()
            viewModel.loadSentShares()
        } else {
            onRequireLogin {}
        }
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ShareActionState.Success -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    snackbarHostState.showSnackbar("Compartilhamento atualizado")
                }
                viewModel.resetActionState()
            }
            is ShareActionState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(s.message) }
                viewModel.resetActionState()
            }
            else -> Unit
        }
    }

    if (currentUser == null) return

    val receivedCount = (receivedState as? ShareUiState.Success)?.shares?.size ?: 0
    val sentCount = (sentState as? ShareUiState.Success)?.shares?.size ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SharesHeader(
                selectedTab = selectedTab,
                receivedCount = receivedCount,
                sentCount = sentCount,
                onTabSelected = { selectedTab = it }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.loadReceivedShares(isRefresh = true)
                    viewModel.loadSentShares(isRefresh = true)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    label = "shares_tab_animation"
                ) { tab ->
                    when (tab) {
                        0 -> ShareList(
                            state = receivedState,
                            movieDetails = movieDetails,
                            isReceived = true,
                            onAccept = viewModel::acceptShare,
                            onReject = viewModel::rejectShare,
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        Text(
            text = "Compartilhamentos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Descubra filmes que seus amigos recomendaram",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(20.dp))

        SharesTabs(
            selectedTab = selectedTab,
            receivedCount = receivedCount,
            sentCount = sentCount,
            onTabSelected = onTabSelected
        )

        Spacer(Modifier.height(8.dp))
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
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(4.dp)
    ) {
        ShareTab(
            title = "Recebidos",
            count = receivedCount,
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        ShareTab(
            title = "Enviados",
            count = sentCount,
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (count > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
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