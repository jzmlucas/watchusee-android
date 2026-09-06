package br.com.watchusee.android.ui.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.FriendRequestResponse
import br.com.watchusee.android.data.dto.FriendResponse
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.viewmodel.SocialActionState
import br.com.watchusee.android.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit,
    viewModel: SocialViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Amigos", "Pedidos")
    
    val friends by viewModel.friends.collectAsStateWithLifecycle()
    val requests by viewModel.requests.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(actionState) {
        if (actionState is SocialActionState.Success) {
            snackbarHostState.showSnackbar((actionState as SocialActionState.Success).message)
            viewModel.resetActionState()
        } else if (actionState is SocialActionState.Error) {
            snackbarHostState.showSnackbar((actionState as SocialActionState.Error).message)
            viewModel.resetActionState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "SOCIAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
                
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
                    divider = {}
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> FriendList(friends = friends)
                1 -> RequestList(
                    requests = requests,
                    onAccept = { viewModel.acceptRequest(it) },
                    onReject = { viewModel.rejectRequest(it) }
                )
            }
            
            if (actionState is SocialActionState.Loading) {
                LoadingState()
            }
        }
    }
}

@Composable
fun FriendList(friends: List<FriendResponse>) {
    if (friends.isEmpty()) {
        EmptyState(
            message = "Você ainda não tem amigos adicionados.",
            icon = Icons.Rounded.Person
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends) { friend ->
                FriendItem(friend = friend)
            }
        }
    }
}

@Composable
fun RequestList(
    requests: List<FriendRequestResponse>,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyState(
            message = "Nenhum pedido de amizade pendente.",
            icon = Icons.Rounded.PersonAdd
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { request ->
                RequestItem(
                    request = request,
                    onAccept = { onAccept(request.id) },
                    onReject = { onReject(request.id) }
                )
            }
        }
    }
}

@Composable
fun FriendItem(friend: FriendResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PremiumGold.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.nick.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = PremiumGold,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = friend.nick,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = TextGrey.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun RequestItem(
    request: FriendRequestResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = request.senderNick.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.senderNick,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Quer ser seu amigo",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = CinemaRed.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Rounded.Close, null, tint = CinemaRed)
                }
                IconButton(
                    onClick = onAccept,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Rounded.Check, null, tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}
