package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.AvatarIconResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.data.repository.MovieRepository
import br.com.watchusee.android.data.repository.SocialRepository
import br.com.watchusee.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Success(
        val profile: UserProfileResponse,
        val recentlyWatched: List<MovieResponse>,
        val friendCount: Int = 0
    ) : ProfileUiState

    data class Error(val message: String) : ProfileUiState
}

sealed interface PasswordActionState {
    data object Idle : PasswordActionState
    data object Loading : PasswordActionState
    data object Success : PasswordActionState
    data class Error(val message: String) : PasswordActionState
}

sealed interface EditProfileActionState {
    data object Idle : EditProfileActionState
    data object Loading : EditProfileActionState
    data object Success : EditProfileActionState
    data class Error(val message: String) : EditProfileActionState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {

    companion object {
        private const val WATCHED_PAGE = 0
        private const val WATCHED_PAGE_SIZE = 20
        private const val RECENT_MOVIES_LIMIT = 10
    }

    private val _uiState =
        MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)

    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    val profile =
        uiState
            .map { state ->
                (state as? ProfileUiState.Success)?.profile
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

    private val _isRefreshing =
        MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> =
        _isRefreshing.asStateFlow()

    private val _passwordActionState =
        MutableStateFlow<PasswordActionState>(
            PasswordActionState.Idle
        )

    val passwordActionState =
        _passwordActionState.asStateFlow()

    private val _editActionState =
        MutableStateFlow<EditProfileActionState>(
            EditProfileActionState.Idle
        )

    val editActionState =
        _editActionState.asStateFlow()

    private val _avatarIcons =
        MutableStateFlow<List<AvatarIconResponse>>(emptyList())

    val avatarIcons =
        _avatarIcons.asStateFlow()

    private val _movieSearchResults =
        MutableStateFlow<List<MovieResponse>>(emptyList())

    val movieSearchResults =
        _movieSearchResults.asStateFlow()

    fun loadProfile(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else if (_uiState.value !is ProfileUiState.Success) {
                _uiState.value = ProfileUiState.Loading
            }

            try {
                val profile =
                    userRepository.getProfile()

                val watchedList =
                    movieRepository.getWatchedList(
                        page = WATCHED_PAGE,
                        size = WATCHED_PAGE_SIZE
                    )

                val friendCount =
                    profile.friendsCount

                val recentlyWatched =
                    watchedList.content
                        .take(RECENT_MOVIES_LIMIT)
                        .map { it.movie }

                _uiState.value =
                    ProfileUiState.Success(
                        profile = profile,
                        recentlyWatched = recentlyWatched,
                        friendCount = friendCount
                    )
            } catch (e: Exception) {
                _uiState.value =
                    ProfileUiState.Error(
                        e.message
                            ?: "Ocorreu um erro desconhecido"
                    )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadAvatarIcons() {
        viewModelScope.launch {
            try {
                val icons =
                    socialRepository.getAvatarIcons()

                if (icons.isEmpty()) {
                    val localIcons =
                        listOf(
                            "POPCORN",
                            "CLAPPERBOARD",
                            "FILM_REEL",
                            "TICKET",
                            "DIRECTOR_CHAIR",
                            "STAR",
                            "COMEDY_MASK",
                            "TRAGEDY_MASK",
                            "GHOST",
                            "ROBOT",
                            "ALIEN",
                            "ASTRONAUT"
                        ).map {
                            AvatarIconResponse(
                                id = it,
                                label = it
                                    .lowercase()
                                    .replaceFirstChar { char ->
                                        char.uppercase()
                                    }
                            )
                        }

                    _avatarIcons.value = localIcons
                } else {
                    _avatarIcons.value = icons
                }
            } catch (e: Exception) {
                val localIcons =
                    listOf(
                        "POPCORN",
                        "CLAPPERBOARD",
                        "FILM_REEL",
                        "TICKET",
                        "DIRECTOR_CHAIR",
                        "STAR",
                        "COMEDY_MASK",
                        "TRAGEDY_MASK",
                        "GHOST",
                        "ROBOT",
                        "ALIEN",
                        "ASTRONAUT"
                    ).map {
                        AvatarIconResponse(
                            id = it,
                            label = it
                                .lowercase()
                                .replaceFirstChar { char ->
                                    char.uppercase()
                                }
                        )
                    }

                _avatarIcons.value = localIcons
            }
        }
    }

    fun searchMoviesForFavorite(query: String) {
        if (query.length < 3) {
            _movieSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _movieSearchResults.value =
                    movieRepository.searchMovies(query)
            } catch (e: Exception) {
                _movieSearchResults.value = emptyList()
            }
        }
    }

    fun updateAvatar(iconId: String?) {
        if (iconId == null) return

        viewModelScope.launch {
            _editActionState.value =
                EditProfileActionState.Loading

            try {
                socialRepository.updateAvatarIcon(iconId)

                _editActionState.value =
                    EditProfileActionState.Success

                loadProfile(true)
            } catch (e: Exception) {
                _editActionState.value =
                    EditProfileActionState.Error(
                        e.message
                            ?: "Erro ao atualizar avatar"
                    )
            }
        }
    }

    fun updateFavoriteMovie(movieId: Long) {
        viewModelScope.launch {
            _editActionState.value =
                EditProfileActionState.Loading

            try {
                socialRepository.updateFavoriteMovie(movieId)

                _editActionState.value =
                    EditProfileActionState.Success

                loadProfile(true)
            } catch (e: Exception) {
                _editActionState.value =
                    EditProfileActionState.Error(
                        e.message
                            ?: "Erro ao atualizar filme favorito"
                    )
            }
        }
    }

    fun removeFavoriteMovie() {
        viewModelScope.launch {
            _editActionState.value =
                EditProfileActionState.Loading

            try {
                socialRepository.removeFavoriteMovie()

                _editActionState.value =
                    EditProfileActionState.Success

                loadProfile(true)
            } catch (e: Exception) {
                _editActionState.value =
                    EditProfileActionState.Error(
                        e.message
                            ?: "Erro ao remover filme favorito"
                    )
            }
        }
    }

    fun resetEditActionState() {
        _editActionState.value =
            EditProfileActionState.Idle
    }

    fun changePassword(
        old: String,
        new: String
    ) {
        viewModelScope.launch {
            _passwordActionState.value =
                PasswordActionState.Loading

            try {
                socialRepository.changePassword(
                    old,
                    new
                )

                _passwordActionState.value =
                    PasswordActionState.Success
            } catch (e: Exception) {
                _passwordActionState.value =
                    PasswordActionState.Error(
                        e.message
                            ?: "Erro ao alterar senha"
                    )
            }
        }
    }

    fun resetPasswordActionState() {
        _passwordActionState.value =
            PasswordActionState.Idle
    }
}