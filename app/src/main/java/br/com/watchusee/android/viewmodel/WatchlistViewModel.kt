package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.WatchlistItemResponse
import br.com.watchusee.android.data.dto.WatchlistSummaryResponse
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WatchlistSortOrder(val displayName: String) {
    RECENT("Mais Recentes"),
    RATING("Melhor Avaliados"),
    TITLE("Título (A-Z)"),
    YEAR("Lançamento")
}

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

    private val _summary = MutableStateFlow<WatchlistSummaryResponse?>(null)
    val summary: StateFlow<WatchlistSummaryResponse?> = _summary.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 20
        private const val LOAD_MORE_THRESHOLD = 4
    }

    private val _toWatchMovies =
        MutableStateFlow<List<WatchlistItemResponse>>(emptyList())

    private val _watchedMovies =
        MutableStateFlow<List<WatchlistItemResponse>>(emptyList())

    private val _toWatchError =
        MutableStateFlow<String?>(null)

    private val _watchedError =
        MutableStateFlow<String?>(null)

    private val _isLoadingToWatch =
        MutableStateFlow(false)

    private val _isLoadingWatched =
        MutableStateFlow(false)

    private val _isLoadingMoreToWatch =
        MutableStateFlow(false)

    private val _isLoadingMoreWatched =
        MutableStateFlow(false)

    private val _query =
        MutableStateFlow("")

    val query: StateFlow<String> =
        _query.asStateFlow()

    private val _isRefreshing =
        MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean> =
        _isRefreshing.asStateFlow()

    private val _sortOrder =
        MutableStateFlow(WatchlistSortOrder.RECENT)

    val sortOrder: StateFlow<WatchlistSortOrder> =
        _sortOrder.asStateFlow()

    private val _toWatchTotalElements =
        MutableStateFlow(0L)

    val toWatchTotalElements: StateFlow<Long> =
        _toWatchTotalElements.asStateFlow()

    private val _watchedTotalElements =
        MutableStateFlow(0L)

    val watchedTotalElements: StateFlow<Long> =
        _watchedTotalElements.asStateFlow()

    private var toWatchPage = 0
    private var watchedPage = 0

    private var toWatchLastPage = false
    private var watchedLastPage = false

    private var lastRemovedMovie: MovieResponse? = null
    private var lastRemovedFromWatched = false

    val toWatchState: StateFlow<WatchlistUiState> =
        combine(
            _toWatchMovies,
            _query,
            _isLoadingToWatch,
            _toWatchError,
            _sortOrder
        ) { movies, q, loading, error, sort ->

            when {
                loading && movies.isEmpty() ->
                    WatchlistUiState.Loading

                error != null ->
                    WatchlistUiState.Error(error)

                else -> {
                    val filtered =
                        if (q.isBlank()) {
                            movies
                        } else {
                            movies.filter {
                                it.movie.title.contains(
                                    q,
                                    ignoreCase = true
                                )
                            }
                        }

                    val sorted =
                        applySorting(
                            filtered,
                            sort
                        )

                    if (sorted.isEmpty()) {
                        WatchlistUiState.Empty
                    } else {
                        WatchlistUiState.Success(sorted)
                    }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WatchlistUiState.Loading
        )

    val watchedState: StateFlow<WatchlistUiState> =
        combine(
            _watchedMovies,
            _query,
            _isLoadingWatched,
            _watchedError,
            _sortOrder
        ) { movies, q, loading, error, sort ->

            when {
                loading && movies.isEmpty() ->
                    WatchlistUiState.Loading

                error != null ->
                    WatchlistUiState.Error(error)

                else -> {
                    val filtered =
                        if (q.isBlank()) {
                            movies
                        } else {
                            movies.filter {
                                it.movie.title.contains(
                                    q,
                                    ignoreCase = true
                                )
                            }
                        }

                    val sorted =
                        applySorting(
                            filtered,
                            sort
                        )

                    if (sorted.isEmpty()) {
                        WatchlistUiState.Empty
                    } else {
                        WatchlistUiState.Success(sorted)
                    }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WatchlistUiState.Loading
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun setSortOrder(order: WatchlistSortOrder) {
        _sortOrder.value = order
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                loadToWatchInternal(refresh = true)
                loadWatchedInternal(refresh = true)
                loadSummaryInternal()
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Erro ao atualizar watchlist",
                    e
                )

                if (
                    _toWatchMovies.value.isEmpty() &&
                    _watchedMovies.value.isEmpty()
                ) {
                    _toWatchError.value =
                        e.message ?: "Não foi possível carregar a watchlist"
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadSummary() {
        viewModelScope.launch {
            runCatching { repository.getWatchlistSummary() }
                .onSuccess { _summary.value = it }
        }
    }

    private suspend fun loadSummaryInternal() {
        _summary.value = repository.getWatchlistSummary()
    }

    fun loadToWatch(
        isRefresh: Boolean = false,
        silent: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            }

            if (!silent && !isRefresh) {
                _isLoadingToWatch.value = true
            }

            try {
                loadToWatchInternal(refresh = isRefresh)
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error loading to watch list",
                    e
                )

                _toWatchError.value =
                    e.message ?: "Erro ao carregar lista"
            } finally {
                _isLoadingToWatch.value = false

                if (isRefresh) {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun loadWatched(
        isRefresh: Boolean = false,
        silent: Boolean = false
    ) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            }

            if (!silent && !isRefresh) {
                _isLoadingWatched.value = true
            }

            try {
                loadWatchedInternal(refresh = isRefresh)
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error loading watched list",
                    e
                )

                _watchedError.value =
                    e.message ?: "Erro ao carregar lista"
            } finally {
                _isLoadingWatched.value = false

                if (isRefresh) {
                    _isRefreshing.value = false
                }
            }
        }
    }

    fun loadMoreToWatch() {
        if (
            toWatchLastPage ||
            _isLoadingMoreToWatch.value ||
            _isLoadingToWatch.value
        ) {
            return
        }

        viewModelScope.launch {
            _isLoadingMoreToWatch.value = true
            _toWatchError.value = null

            try {
                val response =
                    repository.getToWatchList(
                        page = toWatchPage,
                        size = PAGE_SIZE
                    )

                val existingIds =
                    _toWatchMovies.value
                        .map { it.movie.id }
                        .toMutableSet()

                val newItems =
                    response.content.filter { item ->
                        if (existingIds.contains(item.movie.id)) {
                            false
                        } else {
                            existingIds.add(item.movie.id)
                            true
                        }
                    }

                if (newItems.isNotEmpty()) {
                    _toWatchMovies.value =
                        _toWatchMovies.value + newItems
                }

                _toWatchTotalElements.value =
                    response.totalElements

                toWatchLastPage =
                    response.last

                if (response.last) {
                    toWatchPage = response.page
                } else {
                    toWatchPage =
                        response.page + 1
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error loading more to watch movies",
                    e
                )

                _toWatchError.value =
                    e.message ?: "Erro ao carregar mais filmes"
            } finally {
                _isLoadingMoreToWatch.value = false
            }
        }
    }

    fun loadMoreWatched() {
        if (
            watchedLastPage ||
            _isLoadingMoreWatched.value ||
            _isLoadingWatched.value
        ) {
            return
        }

        viewModelScope.launch {
            _isLoadingMoreWatched.value = true
            _watchedError.value = null

            try {
                val response =
                    repository.getWatchedList(
                        page = watchedPage,
                        size = PAGE_SIZE
                    )

                val existingIds =
                    _watchedMovies.value
                        .map { it.movie.id }
                        .toMutableSet()

                val newItems =
                    response.content.filter { item ->
                        if (existingIds.contains(item.movie.id)) {
                            false
                        } else {
                            existingIds.add(item.movie.id)
                            true
                        }
                    }

                if (newItems.isNotEmpty()) {
                    _watchedMovies.value =
                        _watchedMovies.value + newItems
                }

                _watchedTotalElements.value =
                    response.totalElements

                watchedLastPage =
                    response.last

                if (response.last) {
                    watchedPage = response.page
                } else {
                    watchedPage =
                        response.page + 1
                }

            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error loading more watched movies",
                    e
                )

                _watchedError.value =
                    e.message ?: "Erro ao carregar mais filmes"
            } finally {
                _isLoadingMoreWatched.value = false
            }
        }
    }

    private suspend fun loadToWatchInternal(
        refresh: Boolean
    ) {
        _toWatchError.value = null

        if (refresh) {
            toWatchPage = 0
            toWatchLastPage = false
            _toWatchMovies.value = emptyList()
            _toWatchTotalElements.value = 0
        }

        if (toWatchLastPage) {
            return
        }

        val response =
            repository.getToWatchList(
                page = toWatchPage,
                size = PAGE_SIZE
            )

        val newItems =
            if (refresh) {
                response.content.distinctBy {
                    it.movie.id
                }
            } else {
                val existingIds =
                    _toWatchMovies.value
                        .map { it.movie.id }
                        .toSet()

                response.content.filter { item ->
                    item.movie.id !in existingIds
                }
            }

        _toWatchMovies.value =
            if (refresh) {
                newItems
            } else {
                _toWatchMovies.value + newItems
            }

        _toWatchTotalElements.value =
            response.totalElements

        toWatchLastPage =
            response.last

        if (response.last) {
            toWatchPage = response.page
        } else {
            toWatchPage =
                response.page + 1
        }
    }

    private suspend fun loadWatchedInternal(
        refresh: Boolean
    ) {
        _watchedError.value = null

        if (refresh) {
            watchedPage = 0
            watchedLastPage = false
            _watchedMovies.value = emptyList()
            _watchedTotalElements.value = 0
        }

        if (watchedLastPage) {
            return
        }

        val response =
            repository.getWatchedList(
                page = watchedPage,
                size = PAGE_SIZE
            )

        val newItems =
            if (refresh) {
                response.content.distinctBy {
                    it.movie.id
                }
            } else {
                val existingIds =
                    _watchedMovies.value
                        .map { it.movie.id }
                        .toSet()

                response.content.filter { item ->
                    item.movie.id !in existingIds
                }
            }

        _watchedMovies.value =
            if (refresh) {
                newItems
            } else {
                _watchedMovies.value + newItems
            }

        _watchedTotalElements.value =
            response.totalElements

        watchedLastPage =
            response.last

        if (response.last) {
            watchedPage = response.page
        } else {
            watchedPage =
                response.page + 1
        }
    }

    fun shouldLoadMoreToWatch(
        lastVisibleIndex: Int,
        totalItems: Int
    ): Boolean {
        if (totalItems <= 0) {
            return false
        }

        if (toWatchLastPage) {
            return false
        }

        if (_isLoadingMoreToWatch.value) {
            return false
        }

        if (_isLoadingToWatch.value) {
            return false
        }

        return lastVisibleIndex >=
                totalItems - LOAD_MORE_THRESHOLD
    }

    fun shouldLoadMoreWatched(
        lastVisibleIndex: Int,
        totalItems: Int
    ): Boolean {
        if (totalItems <= 0) {
            return false
        }

        if (watchedLastPage) {
            return false
        }

        if (_isLoadingMoreWatched.value) {
            return false
        }

        if (_isLoadingWatched.value) {
            return false
        }

        return lastVisibleIndex >=
                totalItems - LOAD_MORE_THRESHOLD
    }

    fun addToWatch(movieId: Long) {
        viewModelScope.launch {
            try {
                repository.addToWatch(movieId)
                refresh()
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error adding to watch",
                    e
                )
            }
        }
    }

    fun removeFromToWatch(movieId: Long) {
        viewModelScope.launch {
            try {
                val movie =
                    _toWatchMovies.value
                        .find { it.movie.id == movieId }
                        ?.movie

                repository.removeFromToWatch(movieId)

                lastRemovedMovie = movie
                lastRemovedFromWatched = false

                loadToWatch(
                    isRefresh = true,
                    silent = true
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error removing from to watch",
                    e
                )
            }
        }
    }

    fun markAsWatched(movieId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsWatched(movieId)
                refresh()
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error marking as watched",
                    e
                )
            }
        }
    }

    fun removeFromWatched(movieId: Long) {
        viewModelScope.launch {
            try {
                val movie =
                    _watchedMovies.value
                        .find { it.movie.id == movieId }
                        ?.movie

                repository.removeFromWatched(movieId)

                lastRemovedMovie = movie
                lastRemovedFromWatched = true

                loadWatched(
                    isRefresh = true,
                    silent = true
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error removing from watched",
                    e
                )
            }
        }
    }

    fun undoLastRemoval() {
        val movie =
            lastRemovedMovie ?: return

        viewModelScope.launch {
            try {
                if (lastRemovedFromWatched) {
                    repository.markAsWatched(movie.id)

                    loadWatched(
                        isRefresh = true,
                        silent = true
                    )
                } else {
                    repository.addToWatch(movie.id)

                    loadToWatch(
                        isRefresh = true,
                        silent = true
                    )
                }

                lastRemovedMovie = null
            } catch (e: Exception) {
                android.util.Log.e(
                    "WatchlistViewModel",
                    "Error undoing removal",
                    e
                )
            }
        }
    }

    private fun applySorting(
        movies: List<WatchlistItemResponse>,
        sortOrder: WatchlistSortOrder
    ): List<WatchlistItemResponse> {
        return when (sortOrder) {
            WatchlistSortOrder.RECENT ->
                movies.sortedByDescending {
                    it.createdAt
                }

            WatchlistSortOrder.RATING ->
                movies.sortedByDescending {
                    it.movie.rating ?: 0.0
                }

            WatchlistSortOrder.TITLE ->
                movies.sortedBy {
                    it.movie.title
                }

            WatchlistSortOrder.YEAR ->
                movies.sortedByDescending {
                    it.movie.releaseDate ?: ""
                }
        }
    }
}
