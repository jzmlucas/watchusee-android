package br.com.watchusee.android.ui.social

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.SocialActionState
import br.com.watchusee.android.viewmodel.SocialViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    userId: Long,
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit = {},
    viewModel: SocialViewModel = hiltViewModel()
) {
    val profile by viewModel.otherUserProfile.collectAsStateWithLifecycle()
    val recentlyWatched by viewModel.otherUserWatchedMovies.collectAsStateWithLifecycle()
    val friendshipStatus by viewModel.otherUserFriendshipStatus.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadOtherUserProfile(userId)
    }

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
            Column(
                modifier = Modifier
                    .background(DarkNavy)
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PERFIL",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextWhite
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                    }
                }
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            profile?.let { user ->
                OtherUserProfileContent(
                    profile = user,
                    recentlyWatched = recentlyWatched,
                    friendshipStatus = friendshipStatus,
                    onAddFriendClick = { viewModel.sendFriendRequest(user.id) },
                    onAcceptRequestClick = { requestId -> viewModel.acceptFriendshipFromProfile(requestId, user.id) },
                    onRejectRequestClick = { requestId -> viewModel.rejectFriendshipFromProfile(requestId, user.id) },
                    onMovieClick = onMovieClick,
                    isActionLoading = actionState is SocialActionState.Loading
                )
            } ?: run {
                if (actionState is SocialActionState.Loading) {
                    LoadingState()
                } else if (actionState is SocialActionState.Error) {
                    ErrorState(message = (actionState as SocialActionState.Error).message, onRetry = { viewModel.loadOtherUserProfile(userId) })
                }
            }
        }
    }
}

@Composable
private fun OtherUserProfileContent(
    profile: UserProfileResponse,
    recentlyWatched: List<MovieResponse>,
    friendshipStatus: br.com.watchusee.android.data.dto.FriendshipStatusResponse?,
    onAddFriendClick: () -> Unit,
    onAcceptRequestClick: (Long) -> Unit,
    onRejectRequestClick: (Long) -> Unit,
    onMovieClick: (Long) -> Unit,
    isActionLoading: Boolean
) {
    val scrollState = rememberScrollState()
    val lastMovie = recentlyWatched.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(DarkNavy)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            lastMovie?.let { movie ->
                AsyncImage(
                    model = TmdbImageUrl.getBackdropUrl(movie.backdropPath)
                        ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .blur(20.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkNavy.copy(alpha = 0.7f),
                                    DarkNavy
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .background(Color(0xFF1E2433), CircleShape)
                            .border(1.5.dp, PremiumGold, CircleShape)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.avatarIcon != null) {
                            AsyncImage(
                                model = br.com.watchusee.android.util.AvatarMapper.getIconUrl(profile.avatarIcon),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(0.6f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = profile.nick.firstOrNull()?.toString()?.uppercase() ?: "",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp
                                ),
                                color = PremiumGold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStat(label = "Assistidos", value = profile.watchedMovies)
                        ProfileStat(label = "Lista", value = profile.toWatchMovies)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile.nick,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                val formattedDate = remember(profile.createdAt) {
                    try {
                        val zonedDateTime = java.time.ZonedDateTime.parse(profile.createdAt)
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("'Membro desde' MMMM 'de' yyyy", java.util.Locale("pt", "BR"))
                        zonedDateTime.format(formatter)
                    } catch (e: Exception) {
                        "Cinéfilo WatchUsee"
                    }
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(20.dp))
                FriendshipActionArea(
                    statusResponse = friendshipStatus,
                    onAddFriend = onAddFriendClick,
                    onAccept = onAcceptRequestClick,
                    onReject = onRejectRequestClick,
                    isLoading = isActionLoading
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = Color.White.copy(alpha = 0.05f)
        )

        profile.favoriteMovie?.let { movie ->
            FavoriteMovieSection(movie = movie, onClick = { onMovieClick(movie.id) })
        }

        if (recentlyWatched.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VISTOS RECENTEMENTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextGrey.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(recentlyWatched.size) { index ->
                        val movie = recentlyWatched[index]
                        MovieInstaCard(movie, onClick = { onMovieClick(movie.id) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun FriendshipActionArea(
    statusResponse: br.com.watchusee.android.data.dto.FriendshipStatusResponse?,
    onAddFriend: () -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    isLoading: Boolean
) {
    if (statusResponse == null) return

    when (statusResponse.status) {
        "NONE" -> {
            Button(
                onClick = onAddFriend,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Icon(Icons.Rounded.PersonAdd, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADICIONAR AMIGO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
        "FRIENDS" -> {
            Surface(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AMIGOS", fontWeight = FontWeight.Bold, color = TextWhite, letterSpacing = 1.sp)
                    }
                }
            }
        }
        "REQUEST_SENT" -> {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GraySubtle, contentColor = TextGrey),
                enabled = false
            ) {
                Text("SOLICITAÇÃO ENVIADA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
        "REQUEST_RECEIVED" -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { statusResponse.friendshipId?.let { onAccept(it) } },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                    enabled = !isLoading
                ) {
                    Text("ACEITAR", fontWeight = FontWeight.Black)
                }
                OutlinedButton(
                    onClick = { statusResponse.friendshipId?.let { onReject(it) } },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaRed),
                    enabled = !isLoading
                ) {
                    Text("RECUSAR", fontWeight = FontWeight.Black)
                }
            }
        }
        "REJECTED", "SELF" -> { /* No button */ }
    }
}

@Composable
private fun FavoriteMovieSection(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "FILME FAVORITO",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextGrey.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .scaleOnClick(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceGrey)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w185"),
                    contentDescription = null,
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop
                )
                
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = movie.releaseDate?.take(4) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (movie.rating != null && movie.rating > 0) String.format("%.1f", movie.rating) else "N/A",
                            style = MaterialTheme.typography.labelMedium,
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedNumber(
            targetValue = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextWhite,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnimatedNumber(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight = FontWeight.Black
) {
    var startValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        kotlinx.coroutines.delay(300)
        startValue = targetValue
    }

    val animatedValue by animateIntAsState(
        targetValue = startValue,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "number_animation"
    )
    Text(
        text = animatedValue.toString(),
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun MovieInstaCard(movie: MovieResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(165.dp)
            .scaleOnClick(onClick = onClick),
        shape = RoundedCornerShape(4.dp)
    ) {
        AsyncImage(
            model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
