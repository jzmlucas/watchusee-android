package br.com.watchusee.android.ui.social

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.R
import br.com.watchusee.android.data.dto.FriendshipStatusResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.PublicListResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.ui.components.ErrorState
import br.com.watchusee.android.ui.components.LoadingState
import br.com.watchusee.android.viewmodel.ShareActionState
import br.com.watchusee.android.viewmodel.ShareViewModel
import br.com.watchusee.android.viewmodel.SocialActionState
import br.com.watchusee.android.viewmodel.SocialViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

private val Background = Color(0xFF0B0D0F)
private val SurfaceColor = Color(0xFF121212)
private val RaisedColor = Color(0xFF1E1E1E)
private val BorderColor = Color(0xFF2E2E2E)
private val PrimaryText = Color(0xFFE0E0E0)
private val MutedText = Color(0xFF888888)
private val InactiveText = Color(0xFF666666)
private val Lime = Color(0xFFADF03C)
private val ButtonText = Color(0xFF000000)
private val Inter = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold)
)

@Composable
fun FigmaOtherUserProfileScreen(
    userId: Long,
    onBack: () -> Unit,
    onMovieClick: (Long) -> Unit = {},
    viewModel: SocialViewModel = hiltViewModel(),
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    val profile by viewModel.otherUserProfile.collectAsStateWithLifecycle()
    val watchedMovies by viewModel.otherUserWatchedMovies.collectAsStateWithLifecycle()
    val friendship by viewModel.otherUserFriendshipStatus.collectAsStateWithLifecycle()
    val action by viewModel.actionState.collectAsStateWithLifecycle()
    val shareAction by shareViewModel.actionState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    var shareDialog by remember { mutableStateOf(false) }
    var listsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) { viewModel.loadOtherUserProfile(userId) }
    LaunchedEffect(action) {
        when (val state = action) {
            is SocialActionState.Success -> { snackbar.showSnackbar(state.message); viewModel.resetActionState() }
            is SocialActionState.Error -> { snackbar.showSnackbar(state.message); viewModel.resetActionState() }
            else -> Unit
        }
    }
    LaunchedEffect(shareAction) {
        when (val state = shareAction) {
            ShareActionState.Success -> { snackbar.showSnackbar("Filme enviado"); shareViewModel.resetActionState() }
            is ShareActionState.Error -> { snackbar.showSnackbar(state.message); shareViewModel.resetActionState() }
            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar, Modifier.padding(bottom = 12.dp)) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Background)) {
            val user = profile
            when {
                user == null && action is SocialActionState.Loading -> LoadingState()
                user == null && action is SocialActionState.Error -> ErrorState(
                    message = (action as SocialActionState.Error).message,
                    onRetry = { viewModel.loadOtherUserProfile(userId) }
                )
                user == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Lime)
                else -> ProfileContent(
                    profile = user,
                    watchedMovies = watchedMovies,
                    friendship = friendship,
                    loading = action is SocialActionState.Loading,
                    onBack = onBack,
                    onFollow = { viewModel.sendFriendRequest(user.id) },
                    onAccept = { requestId -> viewModel.acceptFriendshipFromProfile(requestId, user.id) },
                    onSendMovie = { shareDialog = true },
                    onLists = { listsDialog = true },
                    onCopyUser = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("WatchuSee", "@${user.nick}")))
                            snackbar.showSnackbar("Usuário copiado")
                        }
                    },
                    onMovieClick = onMovieClick
                )
            }
        }
    }

    if (shareDialog && profile != null) {
        val movies = buildList {
            addAll(watchedMovies)
            profile?.favoriteMovie?.let(::add)
        }.distinctBy(MovieResponse::id)
        ShareMovieDialog(movies, shareAction is ShareActionState.Loading, { shareDialog = false }) { movie ->
            profile?.let { shareViewModel.createShare(movie.id, it.nick, null) }
            shareDialog = false
        }
    }

    if (listsDialog && profile != null) {
        PublicListsDialog(profile!!.publicLists) { listsDialog = false }
    }
}

@Composable
private fun ProfileContent(
    profile: UserProfileResponse,
    watchedMovies: List<MovieResponse>,
    friendship: FriendshipStatusResponse?,
    loading: Boolean,
    onBack: () -> Unit,
    onFollow: () -> Unit,
    onAccept: (Long) -> Unit,
    onSendMovie: () -> Unit,
    onLists: () -> Unit,
    onCopyUser: () -> Unit,
    onMovieClick: (Long) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Background)) {
        CoverHeader(profile.coverUrl, onBack, { menuExpanded = true }, menuExpanded, { menuExpanded = false }, onCopyUser)
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
            Identity(profile)
            profile.bio?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                ProfileText(it, 11, color = MutedText, lineHeight = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            RelationshipActions(friendship, loading, onFollow, onAccept, onSendMovie)
            Spacer(Modifier.height(12.dp))
            Statistics(profile)
            Spacer(Modifier.height(12.dp))
            CommonGenres(profile.commonGenres)
            Spacer(Modifier.height(12.dp))
            FavoriteMovies(profile, watchedMovies, onLists, onMovieClick)
            if (profile.publicLists.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                PublicLists(profile.publicLists, onLists)
            }
        }
    }
}

@Composable
private fun CoverHeader(
    coverUrl: String?,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    menuExpanded: Boolean,
    onDismiss: () -> Unit,
    onCopyUser: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(90.dp)) {
        if (!coverUrl.isNullOrBlank()) {
            AsyncImage(coverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x21080A0C), Background))))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButtonAsset("arrow-left.svg", "Voltar", onBack)
            Box {
                IconButtonAsset("ellipsis.svg", "Mais opções", onMenu)
                DropdownMenu(menuExpanded, onDismiss, modifier = Modifier.background(RaisedColor)) {
                    DropdownMenuItem({ ProfileText("Copiar usuário", 14) }, onCopyUser)
                }
            }
        }
    }
}

@Composable
private fun IconButtonAsset(asset: String, description: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xAB080A0C)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { AssetIcon(asset, Modifier.size(18.dp), description, PrimaryText) }
    }
}

@Composable
private fun Identity(profile: UserProfileResponse) {
    Row(Modifier.fillMaxWidth().height(72.dp), verticalAlignment = Alignment.Bottom) {
        AsyncImage(
            model = br.com.watchusee.android.util.AvatarMapper.getIconUrl(profile.avatarIcon),
            contentDescription = "Foto de perfil",
            modifier = Modifier.size(72.dp).clip(CircleShape),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier.weight(1f).padding(start = 13.dp, bottom = 1.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ProfileText(profile.nick, 20, maxLines = 1)
            ProfileText(buildString {
                append("@${profile.nick}")
                profile.city?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
            }, 10, color = MutedText, maxLines = 1)
            profile.affinityPercent?.let { percent ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssetIcon("sparkles.svg", Modifier.size(13.dp), null, Lime)
                    Spacer(Modifier.width(6.dp))
                    ProfileText("$percent% de afinidade com você", 10, FontWeight.SemiBold, Lime)
                }
            }
        }
    }
}

@Composable
private fun RelationshipActions(
    friendship: FriendshipStatusResponse?,
    loading: Boolean,
    onFollow: () -> Unit,
    onAccept: (Long) -> Unit,
    onSendMovie: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        val status = friendship?.status
        val label = when (status) { "FRIENDS" -> "Amigos"; "REQUEST_SENT" -> "Solicitado"; "REQUEST_RECEIVED" -> "Aceitar"; else -> "Seguir" }
        Surface(
            onClick = { if (status == "REQUEST_RECEIVED") friendship?.friendshipId?.let(onAccept) else onFollow() },
            modifier = Modifier.weight(200f).height(48.dp),
            enabled = !loading && friendship != null && status != "SELF",
            shape = CircleShape,
            color = if (status == "REQUEST_SENT") RaisedColor else Lime
        ) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (loading) CircularProgressIndicator(Modifier.size(17.dp), color = ButtonText, strokeWidth = 2.dp)
                else { AssetIcon("user-plus.svg", Modifier.size(17.dp), null, ButtonText); Spacer(Modifier.width(8.dp)); ProfileText(label, 13, color = ButtonText) }
            }
        }
        Surface(
            onClick = onSendMovie,
            modifier = Modifier.weight(142f).height(48.dp),
            enabled = !loading,
            shape = CircleShape,
            color = RaisedColor,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                AssetIcon("send.svg", Modifier.size(17.dp), null, PrimaryText); Spacer(Modifier.width(8.dp)); ProfileText("Enviar filme", 13)
            }
        }
    }
}

@Composable
private fun Statistics(profile: UserProfileResponse) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceColor).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Stat(profile.watchedMovies.toString(), "assistidos", Modifier.weight(1f))
        DividerVertical()
        Stat(profile.publicLists.size.toString(), "listas públicas", Modifier.weight(1f))
        DividerVertical()
        Stat(profile.friendsCount.toString(), "seguidores", Modifier.weight(1f))
    }
}

@Composable
private fun DividerVertical() { Box(Modifier.width(1.dp).height(28.dp).background(BorderColor)) }

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) { ProfileText(value, 18); ProfileText(label, 9, color = MutedText, maxLines = 1) }
}

@Composable
private fun CommonGenres(genres: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        ProfileText("Vocês gostam de", 20)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            genres.forEachIndexed { index, genre ->
                Surface(shape = CircleShape, color = if (index == 0) Lime else RaisedColor, border = BorderStroke(1.dp, if (index == 0) Lime else BorderColor)) {
                    ProfileText(genre, 11, FontWeight.SemiBold, if (index == 0) ButtonText else MutedText, Modifier.padding(horizontal = 13.dp, vertical = 7.dp))
                }
            }
            if (genres.isEmpty()) ProfileText("Nenhum gênero em comum", 11, color = MutedText, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
private fun FavoriteMovies(profile: UserProfileResponse, watched: List<MovieResponse>, onLists: () -> Unit, onMovieClick: (Long) -> Unit) {
    val movies = buildList { profile.favoriteMovie?.let(::add); addAll(watched) }.distinctBy(MovieResponse::id).take(10)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Filmes favoritos", "Ver todos", onLists)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (movies.isEmpty()) ProfileText("Nenhum filme disponível", 12, color = MutedText)
            movies.forEach { movie -> MovieCard(movie) { onMovieClick(movie.id) } }
        }
    }
}

@Composable
private fun MovieCard(movie: MovieResponse, onClick: () -> Unit) {
    Column(Modifier.width(104.dp).clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(104.dp, 112.dp).shadow(12.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(SurfaceColor)) {
            br.com.watchusee.android.util.TmdbImageUrl.getPosterUrl(movie.posterPath, "w342")?.let { url ->
                AsyncImage(url, movie.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } ?: ProfileText(movie.title, 11, color = MutedText, modifier = Modifier.align(Alignment.Center).padding(8.dp), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        ProfileText(movie.title, 13, FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        ProfileText(movieMetadata(movie), 10, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun movieMetadata(movie: MovieResponse): String = listOfNotNull(movie.genres?.firstOrNull()?.name, movie.releaseDate?.take(4)).joinToString(" · ").ifBlank { "Filme" }

@Composable
private fun PublicLists(lists: List<PublicListResponse>, onLists: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Listas públicas", "Ver ${lists.size}", onLists)
        lists.take(3).forEach { list ->
            Surface(onClick = onLists, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(14.dp), color = RaisedColor) {
                Row(Modifier.padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.size(44.dp, 40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF251F36)), contentAlignment = Alignment.Center) { AssetIcon("moon-star.svg", Modifier.size(18.dp), null, Lime) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        ProfileText(list.name, 12, FontWeight.SemiBold, maxLines = 1)
                        ProfileText("${list.itemCount} filmes · ${list.savesCount} salvamentos", 9, color = MutedText, maxLines = 1)
                    }
                    AssetIcon("bookmark-plus.svg", Modifier.size(16.dp), "Salvar lista", MutedText)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { ProfileText(title, 20); ProfileText(action, 11, FontWeight.SemiBold, Lime, Modifier.clickable(onClick = onClick)) }
}

@Composable
private fun ShareMovieDialog(movies: List<MovieResponse>, loading: Boolean, onDismiss: () -> Unit, onSelect: (MovieResponse) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RaisedColor,
        title = { ProfileText("Enviar filme", 20, FontWeight.SemiBold) },
        text = {
            if (movies.isEmpty()) ProfileText("Nenhum filme disponível para compartilhar.", 14, color = MutedText)
            else Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { movies.take(6).forEach { movie ->
                Row(Modifier.fillMaxWidth().clickable(enabled = !loading) { onSelect(movie) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(br.com.watchusee.android.util.TmdbImageUrl.getPosterUrl(movie.posterPath, "w185"), null, Modifier.size(42.dp, 56.dp).clip(RoundedCornerShape(5.dp)), contentScale = ContentScale.Crop)
                    ProfileText(movie.title, 14, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            } }
        },
        confirmButton = { if (loading) CircularProgressIndicator(Modifier.size(20.dp), Lime, strokeWidth = 2.dp) else TextButton(onDismiss) { ProfileText("Cancelar", 14, FontWeight.SemiBold, Lime) } }
    )
}

@Composable
private fun PublicListsDialog(lists: List<PublicListResponse>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RaisedColor,
        title = { ProfileText("Listas públicas", 20, FontWeight.SemiBold) },
        text = {
            if (lists.isEmpty()) ProfileText("Nenhuma lista pública disponível.", 14, color = MutedText)
            else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { lists.forEach { list ->
                ProfileText(list.name, 14, FontWeight.SemiBold)
                ProfileText("${list.itemCount} filmes · ${list.savesCount} salvamentos", 12, color = MutedText)
            } }
        },
        confirmButton = { TextButton(onDismiss) { ProfileText("Fechar", 14, FontWeight.SemiBold, Lime) } }
    )
}

@Composable
private fun ProfileText(
    text: String,
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    color: Color = PrimaryText,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    lineHeight: androidx.compose.ui.unit.TextUnit = (size * 1.25f).sp
) {
    Text(text = text, modifier = modifier, color = color, fontSize = size.sp, fontFamily = Inter, fontWeight = weight, lineHeight = lineHeight, maxLines = maxLines, overflow = overflow)
}

@Composable
private fun AssetIcon(asset: String, modifier: Modifier, description: String?, tint: Color) {
    AsyncImage(profileAsset(asset), description, modifier, contentScale = ContentScale.Fit, colorFilter = ColorFilter.tint(tint))
}

private fun profileAsset(name: String): String = "file:///android_asset/profile/$name"

@Composable
fun FigmaProfileBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF0B0D0F)) {
        Column(Modifier.fillMaxWidth()) {
            HorizontalDivider(color = Color(0xFF2A3038), thickness = 1.dp)
            Box(Modifier.fillMaxWidth().navigationBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = if (currentRoute == "home") Icons.Rounded.Home else Icons.Outlined.Home,
                        label = "Início",
                        selected = currentRoute == "home",
                        onClick = onHomeClick
                    )
                    BottomNavItem(
                        icon = if (currentRoute == "search") Icons.Rounded.Search else Icons.Outlined.Search,
                        label = "Buscar",
                        selected = currentRoute == "search",
                        onClick = onSearchClick
                    )
                    BottomNavItem(
                        icon = if (currentRoute?.startsWith("library") == true) Icons.Rounded.VideoLibrary else Icons.Outlined.VideoLibrary,
                        label = "Biblioteca",
                        selected = currentRoute?.startsWith("library") == true,
                        onClick = onLibraryClick
                    )
                    BottomNavItem(
                        icon = Icons.Rounded.Person,
                        label = "Perfil",
                        selected = true,
                        onClick = onProfileClick
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = if (selected) Lime else InactiveText
        )
        ProfileText(label, 10, if (selected) FontWeight.SemiBold else FontWeight.Medium, if (selected) Lime else InactiveText, maxLines = 1)
    }
}
