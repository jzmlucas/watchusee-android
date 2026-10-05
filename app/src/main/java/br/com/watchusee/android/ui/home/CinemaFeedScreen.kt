package br.com.watchusee.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.*
import coil3.compose.AsyncImage

@Composable
fun CinemaFeedScreen(
    onMovieClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) { homeViewModel.loadFeaturedMovie(isRefresh = true) }

    Column(
        modifier = Modifier.fillMaxSize().background(CinemaCanvas).verticalScroll(rememberScrollState())
    ) {
        HomeGreeting(currentUser?.nick, onProfileClick)
        SearchField(onSearchClick)
        when (val current = state) {
            is HomeUiState.Success -> {
                current.trendingMovies.firstOrNull()?.let { movie ->
                    FeaturedMovie(movie) { onMovieClick(movie.id) }
                    Spacer(Modifier.height(18.dp))
                }
                MovieRail(
                    title = "Lançamentos",
                    movies = current.nowPlayingMovies.ifEmpty { current.upcomingMovies },
                    onMovieClick = onMovieClick
                )
                (current.recommendedMovies.firstOrNull() ?: current.popularMovies.firstOrNull())?.let { movie ->
                    Spacer(Modifier.height(16.dp))
                    Recommendation(movie) { onMovieClick(movie.id) }
                }
                Spacer(Modifier.height(96.dp))
            }
            is HomeUiState.Error -> FeedError(current.message) { homeViewModel.loadFeaturedMovie() }
            else -> Box(Modifier.fillMaxWidth().height(520.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CinemaAccent)
            }
        }
    }
}

@Composable
private fun HomeGreeting(nick: String?, onProfileClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(CinemaAccent))
                Spacer(Modifier.width(7.dp))
                Text("WatchUsee", color = CinemaText, fontSize = 18.sp)
            }
            Text(
                if (nick.isNullOrBlank()) "Descubra sua próxima cena." else "Olá, $nick. Qual é o filme de hoje?",
                color = CinemaMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(CinemaPanelRaised).clickable(onClick = onProfileClick), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.NotificationsNone, "Notificações", tint = CinemaText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SearchField(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(44.dp)
            .clip(CircleShape).background(CinemaPanelRaised).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, null, tint = CinemaMuted, modifier = Modifier.size(18.dp))
        Text("Buscar filmes, pessoas e listas", color = Color(0xFF5C636C), fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp).weight(1f))
        Icon(Icons.Rounded.Tune, null, tint = CinemaMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun FeaturedMovie(movie: MovieResponse, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(190.dp).clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        AsyncImage(
            model = TmdbImageUrl.getBackdropUrl(movie.backdropPath, "w780") ?: TmdbImageUrl.getPosterUrl(movie.posterPath, "w780"),
            contentDescription = movie.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, CinemaCanvas.copy(alpha = .95f)))))
        Text("DESTAQUE DA SEMANA", color = Color(0xFF0B0D08), fontSize = 9.sp, modifier = Modifier.padding(18.dp).clip(CircleShape).background(CinemaAccent).padding(horizontal = 9.dp, vertical = 5.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp).width(240.dp)) {
            Text(movie.title, color = CinemaText, fontSize = 26.sp, lineHeight = 28.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(movieMeta(movie), color = CinemaMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
        }
        Box(Modifier.align(Alignment.BottomEnd).padding(18.dp).size(42.dp).clip(CircleShape).background(CinemaAccent), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.PlayArrow, "Abrir filme", tint = CinemaCanvas, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MovieRail(title: String, movies: List<MovieResponse>, onMovieClick: (Long) -> Unit) {
    if (movies.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = CinemaText, fontSize = 20.sp, modifier = Modifier.weight(1f))
            Text("Ver todos", color = CinemaAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
            items(movies.take(10), key = { it.id }) { movie ->
                Column(Modifier.width(104.dp).clickable { onMovieClick(movie.id) }) {
                    AsyncImage(model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"), contentDescription = movie.title, modifier = Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                    Text(movie.title, color = CinemaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                    Text(movieMeta(movie), color = CinemaMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun Recommendation(movie: MovieResponse, onClick: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Para o seu momento", color = CinemaText, fontSize = 20.sp, modifier = Modifier.weight(1f))
            Text("Atualizar", color = CinemaAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(14.dp)).background(CinemaPanel).clickable(onClick = onClick).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w185"), contentDescription = movie.title, modifier = Modifier.size(width = 72.dp, height = 46.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Column(Modifier.padding(horizontal = 12.dp).weight(1f)) {
                Text(movie.title, color = CinemaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Recomendado a partir do seu histórico", color = CinemaMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
            Text("›", color = CinemaMuted, fontSize = 24.sp)
        }
    }
}

private fun movieMeta(movie: MovieResponse): String = listOfNotNull(movie.genres?.firstOrNull()?.name, movie.releaseDate?.take(4)).joinToString(" · ").ifBlank { "Filme" }

@Composable
private fun FeedError(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().height(500.dp).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Não foi possível carregar a sua cena", color = CinemaText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(message, color = CinemaMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        Text("Tentar novamente", color = CinemaAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp).clickable(onClick = onRetry))
    }
}
