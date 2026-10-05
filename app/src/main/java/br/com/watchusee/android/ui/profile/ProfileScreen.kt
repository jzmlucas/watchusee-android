package br.com.watchusee.android.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.ProfileSkeleton
import br.com.watchusee.android.ui.animations.scaleOnClick
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ProfileUiState
import br.com.watchusee.android.viewmodel.ProfileViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onWatchedClick: () -> Unit,
    onToWatchClick: () -> Unit,
    onLogout: () -> Unit,
    onRequireLogin: () -> Unit,
    onMovieClick: (Long) -> Unit = {},
    onAboutClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onOtherProfileClick: (Long) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    socialViewModel: br.com.watchusee.android.viewmodel.SocialViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.loadProfile()
        } else if (!authViewModel.isAuthenticated()) {
            onRequireLogin()
        }
    }

    if (currentUser == null) return

    Scaffold(
        containerColor = DarkNavy
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadProfile(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> ProfileSkeleton()

                is ProfileUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() }
                )

                is ProfileUiState.Success -> {
                    ProfileContent(
                        profile = state.profile,
                        recentlyWatched = state.recentlyWatched,
                        friendCount = state.friendCount,
                        onWatchedClick = onWatchedClick,
                        onToWatchClick = onToWatchClick,
                        onFriendsClick = onFriendsClick,
                        onSettingsClick = onSettingsClick,
                        onOtherProfileClick = onOtherProfileClick,
                        onEditProfileClick = onEditProfileClick,
                        onLogoutClick = { showLogoutDialog = true },
                        onMovieClick = onMovieClick,
                        onAboutClick = onAboutClick,
                        socialViewModel = socialViewModel
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceGrey,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Sair da conta?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Você precisará fazer login novamente.",
                    color = TextGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Sair",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextGrey
                    )
                ) {
                    Text("Cancelar")
                }
            }
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
        delay(300)
        startValue = targetValue
    }

    val animatedValue by animateIntAsState(
        targetValue = startValue,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
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
private fun ProfileContent(
    profile: UserProfileResponse,
    recentlyWatched: List<MovieResponse>,
    friendCount: Int,
    onWatchedClick: () -> Unit,
    onToWatchClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOtherProfileClick: (Long) -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onAboutClick: () -> Unit,
    socialViewModel: br.com.watchusee.android.viewmodel.SocialViewModel
) {
    val scrollState = rememberScrollState()
    val movieToDisplay = profile.favoriteMovie

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(DarkNavy)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            movieToDisplay?.let { movie ->
                AsyncImage(
                    model = TmdbImageUrl.getBackdropUrl(movie.backdropPath)
                        ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .blur(24.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DarkNavy.copy(alpha = 0.2f),
                                    DarkNavy.copy(alpha = 0.6f),
                                    DarkNavy
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape
                            )
                            .background(
                                SurfaceGrey,
                                CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = PremiumGold.copy(alpha = 0.85f),
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0xFF1E2433),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.avatarIcon != null) {
                                AsyncImage(
                                    model = br.com.watchusee.android.util.AvatarMapper
                                        .getIconUrl(profile.avatarIcon),
                                    contentDescription = "Avatar de ${profile.nick}",
                                    modifier = Modifier.fillMaxSize(0.65f),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = profile.nick
                                        .firstOrNull()
                                        ?.toString()
                                        ?.uppercase() ?: "",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 30.sp
                                    ),
                                    color = PremiumGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStat(
                            label = "Assistidos",
                            value = profile.watchedMovies,
                            onClick = onWatchedClick
                        )

                        ProfileStat(
                            label = "Lista",
                            value = profile.toWatchMovies,
                            onClick = onToWatchClick
                        )

                        ProfileStat(
                            label = "Amigos",
                            value = friendCount,
                            onClick = onFriendsClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.nick,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )

                val formattedDate = remember(profile.createdAt) {
                    try {
                        val zonedDateTime = ZonedDateTime.parse(profile.createdAt)
                        val formatter = DateTimeFormatter.ofPattern(
                            "'Membro desde' MMMM 'de' yyyy",
                            Locale("pt", "BR")
                        )
                        zonedDateTime.format(formatter)
                    } catch (e: Exception) {
                        "Cinéfilo WatchUsee"
                    }
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey.copy(alpha = 0.65f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileActionButton(
                        text = "Editar perfil",
                        onClick = onEditProfileClick,
                        modifier = Modifier.weight(1f)
                    )

                    ProfileActionButton(
                        text = "Sobre o app",
                        onClick = onAboutClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                UserSearchBar(
                    viewModel = socialViewModel,
                    onUserClick = onOtherProfileClick
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color.White.copy(alpha = 0.06f)
        )

        movieToDisplay?.let { favoriteMovie ->
            FavoriteMovieFeed(
                movie = favoriteMovie,
                onClick = { onMovieClick(favoriteMovie.id) }
            )
        } ?: EmptyFavoriteMovieState()

        if (recentlyWatched.isNotEmpty()) {
            RecentWatchedSection(
                movies = recentlyWatched,
                onMovieClick = onMovieClick,
                onSeeAllClick = onWatchedClick
            )
        }

        SettingsMenu(
            onSettingsClick = onSettingsClick,
            onLogoutClick = onLogoutClick
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun EmptyFavoriteMovieState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Movie,
            contentDescription = null,
            tint = TextGrey.copy(alpha = 0.65f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Escolha um filme favorito no editar perfil",
            color = TextGrey,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FavoriteMovieFeed(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Filme Favorito",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "Destaque do perfil",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = null,
                    tint = TextGrey.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scaleOnClick(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceGrey
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = TmdbImageUrl.getBackdropUrl(movie.backdropPath)
                            ?: TmdbImageUrl.getPosterUrl(
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
                                        Color.Transparent,
                                        SurfaceGrey.copy(alpha = 0.7f),
                                        SurfaceGrey
                                    )
                                )
                            )
                    )

                    Text(
                        text = movie.title,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = PremiumGold,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if ((movie.rating ?: 0.0) > 0.0) {
                                String.format(
                                    Locale.US,
                                    "%.1f",
                                    movie.rating
                                )
                            } else {
                                "—"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    movie.releaseDate
                        ?.takeIf { it.length >= 4 }
                        ?.let {
                            Text(
                                text = it.take(4),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrey
                            )
                        }

                    movie.runtime?.let { runtime ->
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrey.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${runtime} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrey
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Ver detalhes",
                        style = MaterialTheme.typography.labelSmall,
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = PremiumGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextGrey.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGrey.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RecentWatchedSection(
    movies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp,
                bottom = 20.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "VISTOS RECENTEMENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGrey.copy(alpha = 0.65f),
                    letterSpacing = 1.4.sp
                )
            }

            Text(
                text = "Ver todos",
                style = MaterialTheme.typography.labelSmall,
                color = PremiumGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onSeeAllClick()
                    }
                    .padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    )
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(movies.size) { index ->
                val movie = movies[index]

                MovieInstaCard(
                    movie = movie,
                    onClick = {
                        onMovieClick(movie.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsMenu(
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 8.dp
            )
    ) {
        ActivityRow(
            icon = Icons.Rounded.Settings,
            title = "Configurações",
            onClick = onSettingsClick
        )

        ActivityRow(
            icon = Icons.Rounded.Security,
            title = "Segurança",
            onClick = {}
        )

        ActivityRow(
            icon = Icons.AutoMirrored.Rounded.Logout,
            title = "Sair da conta",
            color = CinemaRed,
            onClick = onLogoutClick
        )
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: Int,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.then(
            if (onClick != null) {
                Modifier.clickable {
                    onClick()
                }
            } else {
                Modifier
            }
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
private fun ProfileActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.1f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
private fun MovieInstaCard(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(112.dp)
            .height(168.dp)
            .scaleOnClick(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        AsyncImage(
            model = TmdbImageUrl.getPosterUrl(
                movie.posterPath,
                "w342"
            ),
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ActivityRow(
    icon: ImageVector,
    title: String,
    color: Color = TextWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGrey.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun UserSearchBar(
    viewModel: br.com.watchusee.android.viewmodel.SocialViewModel,
    onUserClick: (Long) -> Unit
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    var isSearching by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                viewModel.onQueryChange(it)
                isSearching = it.length >= 3
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Buscar usuários...",
                    color = TextGrey.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (query.isNotEmpty()) {
                        PremiumGold
                    } else {
                        GraySubtle
                    }
                )
            },
            trailingIcon = {
                if (isSearching && results.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PremiumGold,
                        strokeWidth = 2.dp
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.onQueryChange("")
                            isSearching = false
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = null,
                            tint = TextGrey
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GraySubtle.copy(alpha = 0.2f),
                focusedBorderColor = PremiumGold.copy(alpha = 0.5f),
                cursorColor = PremiumGold,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        if (query.length >= 3 && results.isNotEmpty()) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceGrey.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        results.take(5).forEach { user ->
                            UserSearchResultItem(
                                user = user,
                                onAddClick = {
                                    viewModel.sendFriendRequest(user.id)
                                },
                                onClick = {
                                    onUserClick(user.id)
                                },
                                isLoading = actionState is br.com.watchusee.android.viewmodel.SocialActionState.Loading
                            )

                            if (results.last() != user) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.05f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchResultItem(
    user: br.com.watchusee.android.data.dto.FriendResponse,
    onAddClick: () -> Unit,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 8.dp,
                horizontal = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    PremiumGold.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.nick
                    .take(1)
                    .uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = PremiumGold,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.nick,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite,
                fontWeight = FontWeight.Medium
            )

            if (user.isFriend) {
                Text(
                    text = "Amigo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (user.isFriend) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
            )
        } else {
            IconButton(
                onClick = onAddClick,
                enabled = !isLoading,
                modifier = Modifier.size(32.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = PremiumGold,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        tint = PremiumGold
                    )
                }
            }
        }
    }
}
