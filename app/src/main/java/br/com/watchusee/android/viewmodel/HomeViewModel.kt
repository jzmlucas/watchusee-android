package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistStatusResponse
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
        val movie: MovieResponse,
        val highlights: List<MovieResponse>,
        val status: WatchlistStatusResponse
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    data object Empty : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadFeaturedMovie()
    }

    private var currentHighlightsPage = 1
    private var isLoadingMore = false

    fun loadFeaturedMovie(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
                currentHighlightsPage = 1
            } else {
                _uiState.value = HomeUiState.Loading
            }
            
            try {
                val movie = repository.getRandomTrendingMovie()
                val highlights = try {
                    repository.getTopRatedMovies(page = currentHighlightsPage).filter { it.id != movie.id }
                } catch (e: Exception) {
                    emptyList()
                }
                
                val status = if (authRepository.isAuthenticated()) {
                    try {
                        repository.getWatchlistStatus(movie.id)
                    } catch (e: Exception) {
                        WatchlistStatusResponse(movie.id, false, false)
                    }
                } else {
                    WatchlistStatusResponse(movie.id, false, false)
                }
                _uiState.value = HomeUiState.Success(movie, highlights, status)
            } catch (e: Exception) {
                try {
                    val fallbackMovies = repository.getTopRatedMovies(page = 1)
                    if (fallbackMovies.isNotEmpty()) {
                        val movie = fallbackMovies.random()
                        val highlights = fallbackMovies.filter { it.id != movie.id }
                        val status = if (authRepository.isAuthenticated()) {
                            try {
                                repository.getWatchlistStatus(movie.id)
                            } catch (e: Exception) {
                                WatchlistStatusResponse(movie.id, false, false)
                            }
                        } else {
                            WatchlistStatusResponse(movie.id, false, false)
                        }
                        _uiState.value = HomeUiState.Success(movie, highlights, status)
                    } else {
                        _uiState.value = HomeUiState.Empty
                    }
                } catch (inner: Exception) {
                    _uiState.value = HomeUiState.Error("Não foi possível carregar o destaque.")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMoreHighlights() {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success || isLoadingMore) return

        viewModelScope.launch {
            isLoadingMore = true
            try {
                currentHighlightsPage++
                val newMovies = repository.getTopRatedMovies(page = currentHighlightsPage)
                val filteredMovies = newMovies.filter { it.id != currentState.movie.id }
                
                if (filteredMovies.isNotEmpty()) {
                    val updatedHighlights = currentState.highlights + filteredMovies
                    _uiState.value = currentState.copy(highlights = updatedHighlights)
                }
            } catch (e: Exception) {
                currentHighlightsPage-- // Revert page on failure
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun toggleToWatchlist(movieId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                val currentStatus = currentState.status
                val optimisticStatus = currentStatus.copy(
                    toWatch = !currentStatus.toWatch,
                    watched = if (!currentStatus.toWatch) false else currentStatus.watched
                )
                _uiState.value = currentState.copy(status = optimisticStatus)

                try {
                    if (currentStatus.toWatch) {
                        repository.removeFromToWatch(movieId)
                    } else {
                        if (currentStatus.watched) {
                            repository.removeFromWatched(movieId)
                        }
                        repository.addToWatch(movieId)
                    }
                    refreshStatus(movieId)
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(status = currentStatus)
                }
            }
        }
    }

    fun toggleWatched(movieId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                val currentStatus = currentState.status
                // Optimistic UI update
                val optimisticStatus = currentStatus.copy(
                    watched = !currentStatus.watched,
                    toWatch = if (!currentStatus.watched) false else currentStatus.toWatch
                )
                _uiState.value = currentState.copy(status = optimisticStatus)

                try {
                    if (currentStatus.watched) {
                        repository.removeFromWatched(movieId)
                    } else {
                        if (currentStatus.toWatch) {
                            repository.removeFromToWatch(movieId)
                        }
                        repository.markAsWatched(movieId)
                    }
                    refreshStatus(movieId)
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(status = currentStatus)
                }
            }
        }
    }

    private suspend fun refreshStatus(movieId: Long) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val newStatus = repository.getWatchlistStatus(movieId)
            _uiState.value = currentState.copy(status = newStatus)
        }
    }
}
