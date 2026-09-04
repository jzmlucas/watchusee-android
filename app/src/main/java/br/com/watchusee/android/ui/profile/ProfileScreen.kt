package br.com.watchusee.android.ui.profile

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.ProfileSkeleton
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ProfileUiState
import br.com.watchusee.android.viewmodel.ProfileViewModel
import coil3.compose.AsyncImage
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
    viewModel: ProfileViewModel = hiltViewModel(),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = TextWhite
                    )
                }
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadProfile(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        onWatchedClick = onWatchedClick,
                        onToWatchClick = onToWatchClick,
                        onLogoutClick = { showLogoutDialog = true },
                        onMovieClick = onMovieClick,
                        onAboutClick = onAboutClick
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
                    Text("Sair", fontWeight = FontWeight.Bold)
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
private fun ProfileContent(
    profile: UserProfileResponse,
    recentlyWatched: List<MovieResponse>,
    onWatchedClick: () -> Unit,
    onToWatchClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMovieClick: (Long) -> Unit,
    onAboutClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val lastMovie = recentlyWatched.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        if (lastMovie != null) {
            AsyncImage(
                model = TmdbImageUrl.getBackdropUrl(lastMovie.backdropPath) ?: TmdbImageUrl.getPosterUrl(lastMovie.posterPath, "w780"),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .blur(25.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DarkNavy
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = PremiumGold.copy(alpha = 0.15f),
                        spotColor = PremiumGold.copy(alpha = 0.1f)
                    )
                    .background(
                        Color(0xFF1E2433),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = PremiumGold.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.nick.firstOrNull()?.toString()?.uppercase() ?: "",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = PremiumGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = profile.nick,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp
                ),
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            val formattedDate = remember(profile.createdAt) {
                try {
                    val zonedDateTime = ZonedDateTime.parse(profile.createdAt)
                    val formatter = DateTimeFormatter.ofPattern("'Membro desde' yyyy", Locale("pt", "BR"))
                    zonedDateTime.format(formatter)
                } catch (e: Exception) {
                    "Membro do WatchUsee"
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PremiumGold.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    PremiumGold.copy(alpha = 0.15f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "⭐ $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = PremiumGold.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Assistidos",
                    value = profile.watchedMovies.toString(),
                    icon = Icons.Filled.Visibility,
                    onClick = onWatchedClick,
                    modifier = Modifier.weight(1f),
                    accentColor = PremiumGold
                )
                StatCard(
                    label = "Assistir",
                    value = profile.toWatchMovies.toString(),
                    icon = Icons.Filled.Bookmark,
                    onClick = onToWatchClick,
                    modifier = Modifier.weight(1f),
                    accentColor = AccentBlue
                )
            }

            if (lastMovie != null) {
                Spacer(modifier = Modifier.height(28.dp))

                SectionTitle("ÚLTIMO FILME ASSISTIDO")

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onMovieClick(lastMovie.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceGrey.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        GraySubtle.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = TmdbImageUrl.getPosterUrl(lastMovie.posterPath, "w342"),
                            contentDescription = lastMovie.title,
                            modifier = Modifier
                                .width(68.dp)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = lastMovie.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                maxLines = 2
                            )
                            if (!lastMovie.releaseDate.isNullOrEmpty()) {
                                Text(
                                    text = lastMovie.releaseDate.take(4),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGrey
                                )
                            }
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                            tint = TextGrey.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            if (recentlyWatched.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))

                SectionTitle("VISTOS RECENTEMENTE")

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(recentlyWatched.size) { index ->
                        val movie = recentlyWatched[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(135.dp)
                                    .clickable { onMovieClick(movie.id) }
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = SurfaceGrey
                                )
                            ) {
                                AsyncImage(
                                    model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"),
                                    contentDescription = movie.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrey,
                                maxLines = 1,
                                modifier = Modifier.width(90.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle("MINHA ATIVIDADE")

            ActivityItem(
                icon = Icons.Filled.Movie,
                title = "Filmes Assistidos",
                description = "${profile.watchedMovies} filmes marcados como assistidos",
                onClick = onWatchedClick
            )

            ActivityItem(
                icon = Icons.Filled.BookmarkBorder,
                title = "Minha Lista",
                description = "${profile.toWatchMovies} filmes para assistir",
                onClick = onToWatchClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionTitle("CONFIGURAÇÕES")

            ActivityItem(
                icon = Icons.Outlined.PersonOutline,
                title = "Editar Perfil",
                description = "Personalize suas informações",
                onClick = { /* TODO */ }
            )

            ActivityItem(
                icon = Icons.Outlined.Security,
                title = "Segurança",
                description = "Senha e segurança da conta",
                onClick = { /* TODO */ }
            )

            ActivityItem(
                icon = Icons.Outlined.Info,
                title = "Sobre o App",
                description = "Conheça o desenvolvedor e apoie o projeto",
                onClick = onAboutClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            LogoutButton(onClick = onLogoutClick)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(96.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceGrey.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            GraySubtle.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp
                ),
                fontWeight = FontWeight.Black,
                color = accentColor
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = TextGrey.copy(alpha = 0.6f)
    )
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceGrey.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            GraySubtle.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = PremiumGold.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = PremiumGold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextGrey.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CinemaRed.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            CinemaRed.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = CinemaRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Sair da conta",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CinemaRed
            )
        }
    }
}