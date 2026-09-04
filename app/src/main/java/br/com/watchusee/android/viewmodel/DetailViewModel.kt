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

sealed interface DetailUiState {

    data object Loading : DetailUiState

    data class Success(
        val movie: MovieResponse,
        val status: WatchlistStatusResponse,
        val trailer: br.com.watchusee.android.data.dto.MovieTrailerResponse? = null,
        val relatedMovies: List<MovieResponse> = emptyList(),
        val isLoadingMoreSimilar: Boolean = false,
        val hasMoreSimilarMovies: Boolean = true
    ) : DetailUiState

    data class Error(
        val message: String
    ) : DetailUiState
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DetailUiState>(
            DetailUiState.Loading
        )

    val uiState: StateFlow<DetailUiState> =
        _uiState.asStateFlow()

    private var similarPage = 1

    private var loadingSimilarMovies = false

    private var hasMoreSimilarMovies = true

    fun loadMovieDetail(movieId: Long) {

        viewModelScope.launch {

            _uiState.value =
                DetailUiState.Loading

            similarPage = 1
            loadingSimilarMovies = false
            hasMoreSimilarMovies = true

            try {

                val movie =
                    repository.getMovie(movieId)

                val status =
                    if (authRepository.isAuthenticated()) {
                        try {
                            repository.getWatchlistStatus(movieId)
                        } catch (e: Exception) {
                            WatchlistStatusResponse(movieId, false, false)
                        }
                    } else {
                        WatchlistStatusResponse(
                            movieId,
                            false,
                            false
                        )
                    }

                val relatedMovies =
                    try {

                        repository.getSimilarMovies(
                            movieId = movieId,
                            page = 1
                        )

                    } catch (exception: Exception) {

                        emptyList()
                    }

                val trailer = try {
                    repository.getMovieTrailer(movieId)
                } catch (e: Exception) {
                    null
                }

                if (relatedMovies.isEmpty()) {

                    hasMoreSimilarMovies = false

                } else {

                    similarPage = 1
                    hasMoreSimilarMovies = true
                }

                _uiState.value =
                    DetailUiState.Success(
                        movie = movie,
                        status = status,
                        trailer = trailer,
                        relatedMovies = relatedMovies,
                        isLoadingMoreSimilar = false,
                        hasMoreSimilarMovies = hasMoreSimilarMovies
                    )

            } catch (exception: Exception) {

                _uiState.value =
                    DetailUiState.Error(
                        exception.message
                            ?: "Erro ao carregar detalhes do filme."
                    )
            }
        }
    }

    fun loadMoreSimilarMovies(movieId: Long) {

        if (loadingSimilarMovies) {
            return
        }

        if (!hasMoreSimilarMovies) {
            return
        }

        val currentState =
            _uiState.value

        if (currentState !is DetailUiState.Success) {
            return
        }

        if (currentState.movie.id != movieId) {
            return
        }

        viewModelScope.launch {

            loadingSimilarMovies = true

            _uiState.value =
                currentState.copy(
                    isLoadingMoreSimilar = true
                )

            try {

                val nextPage =
                    similarPage + 1

                val newMovies =
                    repository.getSimilarMovies(
                        movieId = movieId,
                        page = nextPage
                    )

                if (newMovies.isEmpty()) {

                    hasMoreSimilarMovies = false

                    _uiState.value =
                        (_uiState.value as? DetailUiState.Success)
                            ?.copy(
                                isLoadingMoreSimilar = false,
                                hasMoreSimilarMovies = false
                            )!!

                    return@launch
                }

                val existingMovies =
                    currentState.relatedMovies

                val uniqueMovies =
                    newMovies.filter { newMovie ->

                        existingMovies.none {
                                existingMovie ->
                            existingMovie.id == newMovie.id
                        }
                    }

                val updatedMovies =
                    existingMovies + uniqueMovies

                similarPage = nextPage

                if (uniqueMovies.isEmpty()) {
                    hasMoreSimilarMovies = false
                }

                val latestState =
                    _uiState.value

                if (latestState is DetailUiState.Success) {

                    _uiState.value =
                        latestState.copy(
                            relatedMovies = updatedMovies,
                            isLoadingMoreSimilar = false,
                            hasMoreSimilarMovies = hasMoreSimilarMovies
                        )
                }

            } catch (exception: Exception) {

                val latestState =
                    _uiState.value

                if (latestState is DetailUiState.Success) {

                    _uiState.value =
                        latestState.copy(
                            isLoadingMoreSimilar = false
                        )
                }

            } finally {

                loadingSimilarMovies = false
            }
        }
    }

    fun toggleToWatch(
        movieId: Long,
        current: Boolean
    ) {

        viewModelScope.launch {

            val currentState =
                _uiState.value

            if (currentState is DetailUiState.Success) {

                val oldStatus =
                    currentState.status

                val optimisticStatus =
                    oldStatus.copy(
                        toWatch = !current,
                        watched =
                            if (!current) {
                                false
                            } else {
                                oldStatus.watched
                            }
                    )

                _uiState.value =
                    currentState.copy(
                        status = optimisticStatus
                    )

                try {

                    if (current) {

                        repository.removeFromToWatch(
                            movieId
                        )

                    } else {

                        if (oldStatus.watched) {

                            repository.removeFromWatched(
                                movieId
                            )
                        }

                        repository.addToWatch(
                            movieId
                        )
                    }

                    refreshStatus(movieId)

                } catch (exception: Exception) {

                    _uiState.value =
                        currentState.copy(
                            status = oldStatus
                        )
                }
            }
        }
    }

    fun toggleWatched(
        movieId: Long,
        current: Boolean
    ) {

        viewModelScope.launch {

            val currentState =
                _uiState.value

            if (currentState is DetailUiState.Success) {

                val oldStatus =
                    currentState.status

                val optimisticStatus =
                    oldStatus.copy(
                        watched = !current,
                        toWatch =
                            if (!current) {
                                false
                            } else {
                                oldStatus.toWatch
                            }
                    )

                _uiState.value =
                    currentState.copy(
                        status = optimisticStatus
                    )

                try {

                    if (current) {

                        repository.removeFromWatched(
                            movieId
                        )

                    } else {

                        if (oldStatus.toWatch) {

                            repository.removeFromToWatch(
                                movieId
                            )
                        }

                        repository.markAsWatched(
                            movieId
                        )
                    }

                    refreshStatus(movieId)

                } catch (exception: Exception) {

                    _uiState.value =
                        currentState.copy(
                            status = oldStatus
                        )
                }
            }
        }
    }

    private suspend fun refreshStatus(
        movieId: Long
    ) {

        val currentState =
            _uiState.value

        if (currentState is DetailUiState.Success) {

            val newStatus =
                if (authRepository.isAuthenticated()) {
                    try {
                        repository.getWatchlistStatus(movieId)
                    } catch (e: Exception) {
                        WatchlistStatusResponse(movieId, false, false)
                    }
                } else {
                    WatchlistStatusResponse(
                        movieId,
                        false,
                        false
                    )
                }

            _uiState.value =
                currentState.copy(
                    status = newStatus
                )
        }
    }
}