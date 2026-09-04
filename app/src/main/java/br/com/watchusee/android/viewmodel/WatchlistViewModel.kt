package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.WatchlistItemResponse
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WatchlistUiState {
    data object Loading : WatchlistUiState
    data class Success(val items: List<WatchlistItemResponse>) : WatchlistUiState
    data class Error(val message: String) : WatchlistUiState
    data object Empty : WatchlistUiState
}

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _toWatchMovies = MutableStateFlow<List<WatchlistItemResponse>>(emptyList())
    private val _watchedMovies = MutableStateFlow<List<WatchlistItemResponse>>(emptyList())

    private val _toWatchError = MutableStateFlow<String?>(null)
    private val _watchedError = MutableStateFlow<String?>(null)

    private val _isLoadingToWatch = MutableStateFlow(false)
    private val _isLoadingWatched = MutableStateFlow(false)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val toWatchState: StateFlow<WatchlistUiState> = combine(_toWatchMovies, _query, _isLoadingToWatch, _toWatchError) { movies, q, loading, error ->
        when {
            loading && movies.isEmpty() -> WatchlistUiState.Loading
            error != null -> WatchlistUiState.Error(error)
            else -> {
                val filtered = if (q.isBlank()) movies else movies.filter { it.movie.title.contains(q, ignoreCase = true) }
                if (filtered.isEmpty()) WatchlistUiState.Empty else WatchlistUiState.Success(filtered)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WatchlistUiState.Loading)

    val watchedState: StateFlow<WatchlistUiState> = combine(_watchedMovies, _query, _isLoadingWatched, _watchedError) { movies, q, loading, error ->
        when {
            loading && movies.isEmpty() -> WatchlistUiState.Loading
            error != null -> WatchlistUiState.Error(error)
            else -> {
                val filtered = if (q.isBlank()) movies else movies.filter { it.movie.title.contains(q, ignoreCase = true) }
                if (filtered.isEmpty()) WatchlistUiState.Empty else WatchlistUiState.Success(filtered)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WatchlistUiState.Loading)

    init {
        // Carregamento inicial automático
        refresh()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun refresh() {
        loadToWatch(isRefresh = true)
        loadWatched(isRefresh = true)
    }

    fun loadToWatch(isRefresh: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true
            if (!silent && !isRefresh) _isLoadingToWatch.value = true
            
            _toWatchError.value = null
            try {
                // O endpoint GET /watchlist?status=TO_WATCH agora é usado via repository
                val movies = repository.getToWatchList().reversed()
                _toWatchMovies.value = movies
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error loading to watch list", e)
                _toWatchError.value = e.message ?: "Erro ao carregar lista"
            } finally {
                _isLoadingToWatch.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun loadWatched(isRefresh: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true
            if (!silent && !isRefresh) _isLoadingWatched.value = true

            _watchedError.value = null
            try {
                // O endpoint GET /watchlist?status=WATCHED agora é usado via repository
                val movies = repository.getWatchedList().reversed()
                _watchedMovies.value = movies
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error loading watched list", e)
                _watchedError.value = e.message ?: "Erro ao carregar lista"
            } finally {
                _isLoadingWatched.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun addToWatch(movieId: Long) {
        viewModelScope.launch {
            try {
                // Chamada para PUT /watchlist/{movieId} com { "status": "TO_WATCH" }
                repository.addToWatch(movieId)
                refresh() // Atualiza ambas as listas para garantir sincronia
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error adding to watch", e)
            }
        }
    }

    fun removeFromToWatch(movieId: Long) {
        viewModelScope.launch {
            try {
                // Chamada para DELETE /watchlist/{movieId}
                repository.removeFromToWatch(movieId)
                loadToWatch(silent = true)
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error removing from to watch", e)
            }
        }
    }

    fun markAsWatched(movieId: Long) {
        viewModelScope.launch {
            try {
                // Chamada para PUT /watchlist/{movieId} com { "status": "WATCHED" }
                repository.markAsWatched(movieId)
                refresh()
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error marking as watched", e)
            }
        }
    }

    fun removeFromWatched(movieId: Long) {
        viewModelScope.launch {
            try {
                // Chamada para DELETE /watchlist/{movieId}
                repository.removeFromWatched(movieId)
                loadWatched(silent = true)
            } catch (e: Exception) {
                android.util.Log.e("WatchlistViewModel", "Error removing from watched", e)
            }
        }
    }
}
