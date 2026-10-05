package br.com.watchusee.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.ui.cinema.PosterCard
import br.com.watchusee.android.ui.theme.CinemaAccent
import br.com.watchusee.android.ui.theme.CinemaCanvas
import br.com.watchusee.android.ui.theme.CinemaMuted
import br.com.watchusee.android.ui.theme.CinemaPanel
import br.com.watchusee.android.ui.theme.CinemaText
import br.com.watchusee.android.viewmodel.SearchUiState
import br.com.watchusee.android.viewmodel.SearchViewModel

@Composable
fun CinemaExploreScreen(
    onMovieClick: (Long) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(CinemaCanvas)) {
        Text("Explorar", color = CinemaText, fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, "Buscar filmes", tint = CinemaAccent) },
            placeholder = { Text("Filmes, pessoas e listas", color = CinemaMuted) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CinemaPanel,
                unfocusedContainerColor = CinemaPanel,
                focusedBorderColor = CinemaAccent,
                unfocusedBorderColor = CinemaPanel,
                focusedTextColor = CinemaText,
                unfocusedTextColor = CinemaText,
                cursorColor = CinemaAccent
            )
        )
        when (val current = state) {
            is SearchUiState.Success -> MovieGrid(current.movies, onMovieClick)
            is SearchUiState.Idle -> {
                Text("Em alta agora", color = CinemaText, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(20.dp))
                MovieGrid(current.trendingMovies, onMovieClick)
            }
            SearchUiState.Loading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { CircularProgressIndicator(color = CinemaAccent) }
            SearchUiState.Empty -> Text("Nenhum filme encontrado. Tente outro título.", color = CinemaMuted, modifier = Modifier.padding(20.dp))
            is SearchUiState.Error -> Text(current.message, color = CinemaMuted, modifier = Modifier.padding(20.dp))
        }
    }
}

@Composable
private fun MovieGrid(movies: List<br.com.watchusee.android.data.dto.MovieResponse>, onMovieClick: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(movies, key = { it.id }) { movie -> PosterCard(movie, { onMovieClick(movie.id) }, width = 104.dp) }
    }
}
