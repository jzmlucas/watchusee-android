package br.com.watchusee.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.FavoriteMovieResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.PublicListResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.theme.CinemaAccent
import br.com.watchusee.android.ui.theme.CinemaCanvas
import br.com.watchusee.android.ui.theme.CinemaMuted
import br.com.watchusee.android.ui.theme.CinemaPanel
import br.com.watchusee.android.ui.theme.CinemaPanelRaised
import br.com.watchusee.android.ui.theme.CinemaText
import br.com.watchusee.android.util.AvatarMapper
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ProfileUiState
import br.com.watchusee.android.viewmodel.ProfileViewModel
import coil3.compose.AsyncImage

@Composable
fun CinemaProfileScreen(
    onMovieClick: (Long) -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onLogout: () -> Unit,
    onRequireLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        if (currentUser != null) viewModel.loadProfile()
        else if (!authViewModel.isAuthenticated()) onRequireLogin()
    }

    if (currentUser == null) return

    when (val current = state) {
        ProfileUiState.Loading -> ProfileLoading()
        is ProfileUiState.Error -> EmptyState(message = current.message)
        is ProfileUiState.Success -> ProfileContent(
            profile = current.profile,
            recentlyWatched = current.recentlyWatched,
            toWatchMovies = current.toWatchMovies,
            onMovieClick = onMovieClick,
            onEditProfileClick = onEditProfileClick,
            onSettingsClick = onSettingsClick,
            onFriendsClick = onFriendsClick
        )
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfileResponse,
    recentlyWatched: List<MovieResponse>,
    toWatchMovies: List<MovieResponse>,
    onMovieClick: (Long) -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFriendsClick: () -> Unit
) {
    val allUserMovies = (recentlyWatched + toWatchMovies + profile.favoriteMovies.map { MovieResponse(it.id, it.title, null, null, it.posterPath, null, null) }).distinctBy { it.id }

    Column(Modifier.fillMaxSize().background(CinemaCanvas).verticalScroll(rememberScrollState())) {
        ProfileCover(profile, onSettingsClick)
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            ProfileIdentity(profile, onEditProfileClick)
            Text(profile.bio?.takeIf { it.isNotBlank() } ?: "Ainda não há uma bio publicada.", color = CinemaMuted, fontSize = 11.sp, lineHeight = 16.sp)
            ProfileStats(profile, onFriendsClick)
            ProfileGenres((profile.favoriteGenres + profile.commonGenres).distinct())
            FavoriteMovies(profile.favoriteMovies, profile.favoriteMovie, onMovieClick)
            ToWatchMoviesSection(toWatchMovies, onMovieClick)
            MoviesByGenreSection(allUserMovies, onMovieClick)
            ProfileLists(profile.publicLists)
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ProfileCover(profile: UserProfileResponse, onSettingsClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(112.dp)) {
        AsyncImage(model = TmdbImageUrl.getBackdropUrl(profile.coverUrl, "w780"), contentDescription = "Capa do perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .12f), CinemaCanvas))))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("WATCHUSEE", color = CinemaText, fontSize = 13.sp)
            Box(Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = .6f)).clickable(onClick = onSettingsClick), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Settings, "Configurações", tint = CinemaText, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ProfileIdentity(profile: UserProfileResponse, onEditProfileClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        AsyncImage(model = AvatarMapper.getIconUrl(profile.avatarIcon), contentDescription = "Avatar de ${profile.nick}", modifier = Modifier.size(76.dp).clip(CircleShape).background(CinemaAccent), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(profile.nick, color = CinemaText, fontSize = 20.sp)
            Text(buildHandle(profile), color = CinemaMuted, fontSize = 10.sp)
        }
        Button(onClick = onEditProfileClick, colors = ButtonDefaults.buttonColors(containerColor = CinemaPanelRaised), contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(34.dp)) {
            Icon(Icons.Rounded.Edit, null, tint = CinemaText, modifier = Modifier.size(13.dp)); Spacer(Modifier.width(6.dp)); Text("Editar perfil", color = CinemaText, fontSize = 10.sp)
        }
    }
}

private fun buildHandle(profile: UserProfileResponse): String = "@${profile.nick}" + (profile.city?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")

@Composable
private fun ProfileStats(profile: UserProfileResponse, onFriendsClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CinemaPanel).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        ProfileStat(profile.watchedMovies.toString(), "assistidos", Modifier.weight(1f))
        StatDivider()
        ProfileStat(profile.toWatchMovies.toString(), "na lista", Modifier.weight(1f))
        StatDivider()
        Box(Modifier.weight(1f).clickable(onClick = onFriendsClick)) { ProfileStat(profile.friendsCount.toString(), "amigos", Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(value, color = CinemaText, fontSize = 18.sp); Text(label.uppercase(), color = CinemaMuted, fontSize = 9.sp) }
}

@Composable
private fun StatDivider() { Box(Modifier.width(1.dp).height(32.dp).background(Color(0xFF292E35))) }

@Composable
private fun ProfileGenres(genres: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Gêneros favoritos", color = CinemaText, fontSize = 20.sp)
        if (genres.isEmpty()) {
            Text("Os gêneros aparecerão conforme você avaliar filmes.", color = CinemaMuted, fontSize = 11.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { items(genres.distinct().take(8)) { GenreChip(it) } }
        }
    }
}

@Composable
private fun GenreChip(label: String) { Box(Modifier.clip(CircleShape).background(CinemaPanelRaised).padding(horizontal = 13.dp, vertical = 8.dp)) { Text(label, color = CinemaMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) } }

@Composable
private fun FavoriteMovies(favoriteMovies: List<FavoriteMovieResponse>, favoriteMovie: MovieResponse?, onMovieClick: (Long) -> Unit) {
    val movies = favoriteMovies.map { movie -> MovieResponse(movie.id, movie.title, null, null, movie.posterPath, null, null) } + listOfNotNull(favoriteMovie)
    if (movies.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Favoritos de sempre", color = CinemaText, fontSize = 20.sp, modifier = Modifier.weight(1f)) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 4.dp)) { items(movies) { movie -> ProfileMovieCard(movie, onMovieClick) } }
    }
}

@Composable
private fun ToWatchMoviesSection(movies: List<MovieResponse>, onMovieClick: (Long) -> Unit) {
    if (movies.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quero assistir", color = CinemaText, fontSize = 20.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 4.dp)) { items(movies) { movie -> ProfileMovieCard(movie, onMovieClick) } }
    }
}

@Composable
private fun MoviesByGenreSection(allMovies: List<MovieResponse>, onMovieClick: (Long) -> Unit) {
    val moviesByGenre = allMovies.flatMap { movie ->
        val genres = movie.genres?.map { it.name } ?: emptyList()
        if (genres.isEmpty()) listOf("Geral" to movie) else genres.map { genre -> genre to movie }
    }.groupBy({ it.first }, { it.second })

    if (moviesByGenre.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Filmes por gênero", color = CinemaText, fontSize = 20.sp)
        moviesByGenre.forEach { (genre, movies) ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(genre, color = CinemaAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 4.dp)) {
                    items(movies.distinctBy { it.id }) { movie -> ProfileMovieCard(movie, onMovieClick) }
                }
            }
        }
    }
}

@Composable
private fun ProfileMovieCard(movie: MovieResponse, onMovieClick: (Long) -> Unit) {
    Column(Modifier.width(104.dp).clickable { onMovieClick(movie.id) }, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        AsyncImage(model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"), contentDescription = movie.title, modifier = Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
        Text(movie.title, color = CinemaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(movie.releaseDate?.take(4) ?: "Filme", color = CinemaMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ProfileLists(lists: List<PublicListResponse>) {
    if (lists.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Minhas listas", color = CinemaText, fontSize = 20.sp, modifier = Modifier.weight(1f)); Text("Ver ${lists.size}", color = CinemaAccent, fontSize = 11.sp) }
        lists.take(5).forEach { ProfileListCard(it) }
    }
}

@Composable
private fun ProfileListCard(list: PublicListResponse) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CinemaPanelRaised).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(CinemaPanel))
        Column(Modifier.weight(1f).padding(horizontal = 11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(list.name, color = CinemaText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("${list.itemCount} filmes", color = CinemaMuted, fontSize = 9.sp) }
        Icon(Icons.Rounded.ChevronRight, "Abrir lista", tint = CinemaMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ProfileLoading() { Box(Modifier.fillMaxSize().background(CinemaCanvas), contentAlignment = Alignment.Center) { Text("Carregando perfil", color = CinemaMuted) } }
