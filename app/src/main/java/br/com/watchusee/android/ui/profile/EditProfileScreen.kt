package br.com.watchusee.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.AvatarIconResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.viewmodel.EditProfileActionState
import br.com.watchusee.android.viewmodel.ProfileViewModel
import br.com.watchusee.android.viewmodel.ProfileUiState
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val actionState by viewModel.editActionState.collectAsStateWithLifecycle()
    val avatarIcons by viewModel.avatarIcons.collectAsStateWithLifecycle()
    val movieResults by viewModel.movieSearchResults.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var movieQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadAvatarIcons()
    }

    LaunchedEffect(actionState) {
        if (actionState is EditProfileActionState.Success) {
            snackbarHostState.showSnackbar("Perfil atualizado!")
            viewModel.resetEditActionState()
        } else if (actionState is EditProfileActionState.Error) {
            snackbarHostState.showSnackbar((actionState as EditProfileActionState.Error).message)
            viewModel.resetEditActionState()
        }
    }

    LaunchedEffect(movieQuery) {
        if (movieQuery.length >= 3) {
            viewModel.searchMoviesForFavorite(movieQuery)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(modifier = Modifier.background(DarkNavy).statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "EDITAR PERFIL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkNavy
                    )
                )
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "Escolha seu Avatar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (avatarIcons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PremiumGold
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(avatarIcons) { icon ->
                        AvatarItem(
                            icon = icon,
                            isSelected = (uiState as? ProfileUiState.Success)?.profile?.avatarIcon == icon.id,
                            onClick = { viewModel.updateAvatar(icon.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Filme Favorito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )

            val currentFavorite = (uiState as? ProfileUiState.Success)?.profile?.favoriteMovie

            when (uiState) {
                is ProfileUiState.Loading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceGrey.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PremiumGold,
                            strokeWidth = 2.dp
                        )
                    }
                }
                is ProfileUiState.Success -> {
                    if (currentFavorite != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CurrentFavoriteCard(
                            movie = currentFavorite,
                            onRemove = { viewModel.removeFavoriteMovie() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhum filme favorito selecionado",
                            color = TextGrey,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = movieQuery,
                onValueChange = {
                    movieQuery = it
                    if (it.isEmpty()) {
                        viewModel.searchMoviesForFavorite("")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Buscar novo filme favorito...",
                        color = TextGrey.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = if (movieQuery.isNotEmpty()) PremiumGold else GraySubtle
                    )
                },
                trailingIcon = {
                    if (movieQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            movieQuery = ""
                            viewModel.searchMoviesForFavorite("")
                        }) {
                            Icon(
                                Icons.Rounded.Close,
                                null,
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
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = PremiumGold
                )
            )

            // Search Results
            if (movieResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceGrey.copy(alpha = 0.3f)
                    )
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movieResults) { movie ->
                            MovieSearchItem(
                                movie = movie,
                                onClick = {
                                    viewModel.updateFavoriteMovie(movie.id)
                                    movieQuery = ""
                                    viewModel.searchMoviesForFavorite("")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (actionState is EditProfileActionState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkNavy
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PremiumGold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Atualizando perfil...",
                            color = TextWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarItem(
    icon: AvatarIconResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) PremiumGold.copy(alpha = 0.15f)
                else GraySubtle.copy(alpha = 0.1f)
            )
            .border(
                2.dp,
                if (isSelected) PremiumGold else GraySubtle.copy(alpha = 0.2f),
                CircleShape
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = br.com.watchusee.android.util.AvatarMapper.getIconUrl(icon.id),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(PremiumGold)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    tint = DarkNavy,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CurrentFavoriteCard(
    movie: MovieResponse,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(width = 60.dp, height = 90.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = br.com.watchusee.android.util.TmdbImageUrl.getPosterUrl(movie.posterPath, "w185"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    movie.releaseDate?.take(4) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    null,
                    tint = CinemaRed
                )
            }
        }
    }
}

@Composable
fun MovieSearchItem(
    movie: MovieResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(2f/3f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceGrey
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = br.com.watchusee.android.util.TmdbImageUrl.getPosterUrl(movie.posterPath, "w185"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}