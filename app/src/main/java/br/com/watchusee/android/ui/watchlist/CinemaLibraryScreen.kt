package br.com.watchusee.android.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistItemResponse
import br.com.watchusee.android.data.dto.WatchlistSummaryResponse
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.theme.CinemaAccent
import br.com.watchusee.android.ui.theme.CinemaCanvas
import br.com.watchusee.android.ui.theme.CinemaMuted
import br.com.watchusee.android.ui.theme.CinemaPanel
import br.com.watchusee.android.ui.theme.CinemaPanelRaised
import br.com.watchusee.android.ui.theme.CinemaText
import br.com.watchusee.android.util.TmdbImageUrl
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.WatchlistSortOrder
import br.com.watchusee.android.viewmodel.WatchlistUiState
import br.com.watchusee.android.viewmodel.WatchlistViewModel
import coil3.compose.AsyncImage

private val WatchedColor = Color(0xFFB9A7FF)

@Composable
fun CinemaLibraryScreen(
    initialTab: Int = 0,
    onMovieClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onRequireLogin: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val toWatch by viewModel.toWatchState.collectAsStateWithLifecycle()
    val watched by viewModel.watchedState.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val toWatchTotal by viewModel.toWatchTotalElements.collectAsStateWithLifecycle()
    val watchedTotal by viewModel.watchedTotalElements.collectAsStateWithLifecycle()
    var tab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) viewModel.refresh()
        else if (!authViewModel.isAuthenticated()) onRequireLogin()
    }

    if (currentUser == null) return

    val selectedState = if (tab == 0) toWatch else watched
    val selectedColor = if (tab == 0) CinemaAccent else WatchedColor
    val selectedItems = (selectedState as? WatchlistUiState.Success)?.items.orEmpty()

    Column(Modifier.fillMaxSize().background(CinemaCanvas)) {
        LibraryHeader(
            total = summary?.totalCount ?: (toWatchTotal + watchedTotal),
            onFilterClick = {
                viewModel.setSortOrder(if (sortOrder == WatchlistSortOrder.RECENT) WatchlistSortOrder.YEAR else WatchlistSortOrder.RECENT)
            }
        )
        StatusSegment(
            selectedTab = tab,
            toWatchCount = summary?.toWatchCount ?: toWatchTotal,
            watchedCount = summary?.watchedCount ?: watchedTotal,
            onSelected = { tab = it }
        )
        LibrarySummary(summary, selectedColor)
        SortChips(sortOrder, viewModel::setSortOrder)

        when (selectedState) {
            WatchlistUiState.Loading -> LibraryLoadingGrid()
            is WatchlistUiState.Error -> ErrorState(
                message = selectedState.message,
                onRetry = { viewModel.refresh() }
            )
            WatchlistUiState.Empty -> EmptyState(
                message = if (tab == 0) "Sua lista de filmes para assistir está vazia." else "Você ainda não marcou filmes como assistidos.",
                actionLabel = "Buscar filmes",
                onAction = onNavigateToSearch
            )
            is WatchlistUiState.Success -> LibraryGrid(selectedItems, selectedColor, onMovieClick)
        }
    }
}

@Composable
private fun LibraryHeader(total: Long, onFilterClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("$total filmes", color = CinemaAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text("Sua biblioteca", color = CinemaText, fontSize = 28.sp)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(CinemaPanelRaised).clickable(onClick = onFilterClick), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Tune, contentDescription = "Alternar ordenação", tint = CinemaText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StatusSegment(selectedTab: Int, toWatchCount: Long, watchedCount: Long, onSelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(CircleShape).background(CinemaPanel).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Segment("Quero assistir · $toWatchCount", selectedTab == 0, CinemaAccent, Modifier.weight(1f)) { onSelected(0) }
        Segment("Já assisti · $watchedCount", selectedTab == 1, WatchedColor, Modifier.weight(1f)) { onSelected(1) }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.height(34.dp).clip(CircleShape).background(if (selected) color else Color.Transparent).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) CinemaCanvas else CinemaMuted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.SemiBold)
    }
}

@Composable
private fun LibrarySummary(summary: WatchlistSummaryResponse?, color: Color) {
    val percentage = if (summary == null) 0 else summary.monthWatchedPercentage
    val detail = if (summary == null) "Carregando seus dados" else "${summary.watchedThisMonth} de ${summary.trackedThisMonth} filmes acompanhados"
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).clip(RoundedCornerShape(14.dp)).background(CinemaPanelRaised).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Resumo deste mês", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = CinemaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).background(CinemaCanvas), contentAlignment = Alignment.Center) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(CinemaPanelRaised), contentAlignment = Alignment.Center) {
                    Text("$percentage%", color = CinemaText, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SortChips(sortOrder: WatchlistSortOrder, onSortSelected: (WatchlistSortOrder) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SortChip("Todos", sortOrder == WatchlistSortOrder.RECENT) { onSortSelected(WatchlistSortOrder.RECENT) }
        SortChip("Melhor avaliados", sortOrder == WatchlistSortOrder.RATING) { onSortSelected(WatchlistSortOrder.RATING) }
        SortChip("Mais recentes", sortOrder == WatchlistSortOrder.YEAR) { onSortSelected(WatchlistSortOrder.YEAR) }
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).background(if (selected) CinemaAccent else CinemaPanelRaised).clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 8.dp)) {
        Text(label, color = if (selected) CinemaCanvas else CinemaMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun LibraryGrid(items: List<WatchlistItemResponse>, color: Color, onMovieClick: (Long) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(items, key = { it.movie.id }) { item -> LibraryMovieCard(item.movie, color, onMovieClick) }
    }
}

@Composable
private fun LibraryMovieCard(movie: MovieResponse, color: Color, onMovieClick: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onMovieClick(movie.id) }, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.fillMaxWidth().height(126.dp).clip(RoundedCornerShape(14.dp)).background(CinemaPanel)) {
            AsyncImage(model = TmdbImageUrl.getPosterUrl(movie.posterPath, "w342"), contentDescription = movie.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.align(Alignment.TopStart).padding(7.dp).size(7.dp).clip(CircleShape).background(color))
        }
        Text(movie.title, color = CinemaText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(movieMetadata(movie), color = CinemaMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun movieMetadata(movie: MovieResponse): String {
    val genre = movie.genres?.firstOrNull()?.name
    val year = movie.releaseDate?.take(4)
    return listOfNotNull(genre, year).joinToString(" · ").ifBlank { "Filme" }
}

@Composable
private fun LibraryLoadingGrid() {
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(6) { Box(Modifier.fillMaxWidth().height(126.dp).clip(RoundedCornerShape(14.dp)).background(CinemaPanelRaised)) }
    }
}
