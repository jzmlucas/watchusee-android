package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.repository.AuthRepository
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val trendingMovies: List<MovieResponse>,
        val popularMovies: List<MovieResponse> = emptyList(),
        val nowPlayingMovies: List<MovieResponse> = emptyList(),
        val upcomingMovies: List<MovieResponse> = emptyList(),
        val toWatchList: List<MovieResponse> = emptyList(),
        val watchedList: List<MovieResponse> = emptyList(),
        val recommendedMovies: List<MovieResponse> = emptyList(),
        val watchlistStatusMap: Map<Long, String> = emptyMap(),
        val loadingMovieIds: Set<Long> = emptySet()
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState

    data object Empty : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val WATCHLIST_PAGE_SIZE = 20
    }

    private val _uiState =
        MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private val _isRefreshing =
        MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> =
        _isRefreshing.asStateFlow()

    private var currentHighlightsPage = 1

    init {
        loadFeaturedMovie()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect {
                loadFeaturedMovie()
            }
        }
    }

    fun loadFeaturedMovie(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
                currentHighlightsPage = 1
            } else {
                _uiState.value = HomeUiState.Loading
            }

            try {
                val trending = try {
                    val result = repository.getTrendingMovies()

                    if (result.isEmpty()) {
                        repository.getPopularMovies(1).results
                    } else {
                        result
                    }
                } catch (e: Exception) {
                    repository.getPopularMovies(1).results
                }

                val popular = try {
                    repository.getPopularMovies(1).results
                } catch (e: Exception) {
                    emptyList()
                }

                val nowPlaying = try {
                    repository.getNowPlayingMovies(1).results
                } catch (e: Exception) {
                    emptyList()
                }

                val upcoming = try {
                    repository.getUpcomingMovies(1).results
                } catch (e: Exception) {
                    emptyList()
                }

                val toWatch = if (authRepository.isAuthenticated()) {
                    try {
                        repository
                            .getToWatchList(
                                page = 0,
                                size = WATCHLIST_PAGE_SIZE
                            )
                            .content
                            .map { it.movie }
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val watched = if (authRepository.isAuthenticated()) {
                    try {
                        repository
                            .getWatchedList(
                                page = 0,
                                size = WATCHLIST_PAGE_SIZE
                            )
                            .content
                            .map { it.movie }
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val statusMap = mutableMapOf<Long, String>()

                toWatch.forEach {
                    statusMap[it.id] = "TO_WATCH"
                }

                watched.forEach {
                    statusMap[it.id] = "WATCHED"
                }

                val firstMovieId =
                    trending.firstOrNull()?.id

                val recommended =
                    if (firstMovieId != null) {
                        try {
                            repository
                                .getRecommendations(
                                    firstMovieId,
                                    1
                                )
                                .results
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }

                _uiState.value = HomeUiState.Success(
                    trendingMovies = trending,
                    popularMovies = popular,
                    nowPlayingMovies = nowPlaying,
                    upcomingMovies = upcoming,
                    toWatchList = toWatch,
                    watchedList = watched,
                    recommendedMovies = recommended,
                    watchlistStatusMap = statusMap
                )
            } catch (e: Exception) {
                _uiState.value =
                    HomeUiState.Error(
                        "Não foi possível carregar o conteúdo."
                    )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleWatchlist(
        movieId: Long,
        targetStatus: String
    ) {
        val currentState =
            _uiState.value as? HomeUiState.Success
                ?: return

        if (movieId in currentState.loadingMovieIds) {
            return
        }

        viewModelScope.launch {
            val currentStatus =
                currentState.watchlistStatusMap[movieId]

            val isRemoving =
                currentStatus == targetStatus

            val newMap =
                currentState.watchlistStatusMap.toMutableMap()

            if (isRemoving) {
                newMap.remove(movieId)
            } else {
                newMap[movieId] = targetStatus
            }

            _uiState.value =
                currentState.copy(
                    watchlistStatusMap = newMap,
                    loadingMovieIds =
                        currentState.loadingMovieIds + movieId
                )

            try {
                if (isRemoving) {
                    if (targetStatus == "TO_WATCH") {
                        repository.removeFromToWatch(movieId)
                    } else {
                        repository.removeFromWatched(movieId)
                    }
                } else {
                    if (targetStatus == "TO_WATCH") {
                        repository.addToWatch(movieId)
                    } else {
                        repository.markAsWatched(movieId)
                    }
                }

                refreshWatchlistStatus()
            } catch (e: Exception) {
                val revertedState =
                    _uiState.value as? HomeUiState.Success

                if (revertedState != null) {
                    _uiState.value =
                        revertedState.copy(
                            watchlistStatusMap =
                                currentState.watchlistStatusMap,
                            loadingMovieIds =
                                revertedState.loadingMovieIds - movieId
                        )
                }
            } finally {
                val finalState =
                    _uiState.value as? HomeUiState.Success

                if (finalState != null) {
                    _uiState.value =
                        finalState.copy(
                            loadingMovieIds =
                                finalState.loadingMovieIds - movieId
                        )
                }
            }
        }
    }

    private suspend fun refreshWatchlistStatus() {
        if (!authRepository.isAuthenticated()) {
            return
        }

        try {
            val toWatch =
                repository
                    .getToWatchList(
                        page = 0,
                        size = WATCHLIST_PAGE_SIZE
                    )
                    .content
                    .map { it.movie }

            val watched =
                repository
                    .getWatchedList(
                        page = 0,
                        size = WATCHLIST_PAGE_SIZE
                    )
                    .content
                    .map { it.movie }

            val statusMap =
                mutableMapOf<Long, String>()

            toWatch.forEach {
                statusMap[it.id] = "TO_WATCH"
            }

            watched.forEach {
                statusMap[it.id] = "WATCHED"
            }

            val currentState =
                _uiState.value as? HomeUiState.Success

            if (currentState != null) {
                _uiState.value =
                    currentState.copy(
                        watchlistStatusMap = statusMap,
                        toWatchList = toWatch,
                        watchedList = watched
                    )
            }
        } catch (e: Exception) {
        }
    }

    fun loadMoreHighlights() {
    }
}