package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistStatusResponse
import br.com.watchusee.android.data.repository.AuthRepository
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface SearchUiState {
    data class Idle(val trendingMovies: List<MovieResponse> = emptyList()) : SearchUiState
    data object Loading : SearchUiState
    data class Success(
        val movies: List<MovieResponse>,
        val statuses: Map<Long, WatchlistStatusResponse> = emptyMap()
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var trendingMovies: List<MovieResponse> = emptyList()

    init {
        fetchTrendingMovies()
        viewModelScope.launch {
            _query
                .debounce(500.milliseconds)
                .distinctUntilChanged()
                .collectLatest { q ->
                    performSearch(q)
                }
        }
    }

    private fun fetchTrendingMovies() {
        viewModelScope.launch {
            try {
                trendingMovies = repository.getTrendingMovies()
                if (_uiState.value is SearchUiState.Idle) {
                    _uiState.value = SearchUiState.Idle(trendingMovies)
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Erro ao buscar tendências: ${e.message}", e)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle(trendingMovies)
        }
    }

    private suspend fun performSearch(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            _uiState.value = SearchUiState.Idle(trendingMovies)
            return
        }

        _uiState.value = SearchUiState.Loading
        try {
            val movies = repository.searchMovies(normalizedQuery)
            if (movies.isEmpty()) {
                _uiState.value = SearchUiState.Empty
            } else {
                // Fetch statuses in parallel
                coroutineScope {
                    val statusMap = movies.map { movie ->
                        async {
                            val status = if (authRepository.isAuthenticated()) {
                                try {
                                    repository.getWatchlistStatus(movie.id)
                                } catch (e: Exception) {
                                    WatchlistStatusResponse(movie.id, false, false)
                                }
                            } else {
                                WatchlistStatusResponse(movie.id, false, false)
                            }
                            movie.id to status
                        }
                    }.awaitAll().toMap()

                    _uiState.value = SearchUiState.Success(movies, statusMap)
                }
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(e.message ?: "Erro inesperado")
        }
    }

    fun searchMovies(query: String) {
        onQueryChange(query)
    }

    fun toggleToWatch(movieId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SearchUiState.Success) {
                val currentStatus = currentState.statuses[movieId] ?: WatchlistStatusResponse(movieId, false, false)

                val optimisticStatus = currentStatus.copy(
                    toWatch = !currentStatus.toWatch,
                    watched = if (!currentStatus.toWatch) false else currentStatus.watched
                )
                updateLocalStatus(movieId, optimisticStatus)

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
                    updateLocalStatus(movieId, currentStatus)
                }
            }
        }
    }

    fun toggleWatched(movieId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SearchUiState.Success) {
                val currentStatus = currentState.statuses[movieId] ?: WatchlistStatusResponse(movieId, false, false)

                val optimisticStatus = currentStatus.copy(
                    watched = !currentStatus.watched,
                    toWatch = if (!currentStatus.watched) false else currentStatus.toWatch
                )
                updateLocalStatus(movieId, optimisticStatus)

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
                    updateLocalStatus(movieId, currentStatus)
                }
            }
        }
    }

    private fun updateLocalStatus(movieId: Long, status: WatchlistStatusResponse) {
        val currentState = _uiState.value
        if (currentState is SearchUiState.Success) {
            val newStatuses = currentState.statuses.toMutableMap()
            newStatuses[movieId] = status
            _uiState.value = currentState.copy(statuses = newStatuses)
        }
    }

    private suspend fun refreshStatus(movieId: Long) {
        val currentState = _uiState.value
        if (currentState is SearchUiState.Success) {
            val newStatus = try {
                repository.getWatchlistStatus(movieId)
            } catch (e: Exception) {
                WatchlistStatusResponse(movieId, false, false)
            }
            updateLocalStatus(movieId, newStatus)
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState.Idle(trendingMovies)
    }
}