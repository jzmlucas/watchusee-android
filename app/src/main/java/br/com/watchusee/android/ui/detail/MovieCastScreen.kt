package br.com.watchusee.android.ui.detail

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import br.com.watchusee.android.data.dto.MovieCastMemberResponse
import br.com.watchusee.android.ui.components.EmptyState
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.theme.CinemaAccent
import br.com.watchusee.android.ui.theme.CinemaCanvas
import br.com.watchusee.android.ui.theme.CinemaMuted
import br.com.watchusee.android.ui.theme.CinemaPanel
import br.com.watchusee.android.ui.theme.CinemaText
import br.com.watchusee.android.viewmodel.CastUiState
import br.com.watchusee.android.viewmodel.CastViewModel
import br.com.watchusee.android.util.TmdbImageUrl
import coil3.compose.AsyncImage

@Composable
fun MovieCastScreen(
    movieId: Long,
    onBack: () -> Unit,
    viewModel: CastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.loadCast(movieId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaCanvas)
    ) {
        CastTopBar(onBack)

        when (val state = uiState) {
            CastUiState.Loading -> CastLoading()
            is CastUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.loadCast(movieId) }
            )
            is CastUiState.Success -> {
                if (state.cast.isEmpty()) {
                    EmptyState(
                        message = "Nenhum ator encontrado para este filme.",
                        icon = Icons.Rounded.People
                    )
                } else {
                    CastGrid(state.cast)
                }
            }
        }
    }
}

@Composable
private fun CastTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CinemaPanel)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = CinemaText)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text("Elenco", color = CinemaText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("Atores do filme", color = CinemaMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CastGrid(cast: List<MovieCastMemberResponse>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(cast, key = { "${it.id}-${it.name}" }) { person ->
            CastCard(person)
        }
    }
}

@Composable
private fun CastCard(person: MovieCastMemberResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CinemaPanel)
            .padding(bottom = 12.dp)
    ) {
        AsyncImage(
            model = TmdbImageUrl.getPosterUrl(person.profilePath, "w342"),
            contentDescription = person.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = person.name,
            color = CinemaText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        )
        Text(
            text = person.character?.takeIf { it.isNotBlank() } ?: "Personagem não informado",
            color = CinemaMuted,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun CastLoading() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(6) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CinemaPanel)
                    .padding(bottom = 12.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(190.dp).background(Color(0xFF20252B)))
                Box(Modifier.padding(12.dp).fillMaxWidth().height(14.dp).background(Color(0xFF20252B)))
                Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth(.65f).height(11.dp).background(Color(0xFF20252B)))
            }
        }
    }
}
