package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.ShareResponse
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed interface ShareUiState {
    data object Idle : ShareUiState
    data object Loading : ShareUiState
    data class Success(val shares: List<ShareResponse>) : ShareUiState
    data class Error(val message: String) : ShareUiState
}

sealed interface ShareActionState {
    data object Idle : ShareActionState
    data object Loading : ShareActionState
    data object Success : ShareActionState
    data class Error(val message: String) : ShareActionState
}

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _receivedState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val receivedState: StateFlow<ShareUiState> = _receivedState.asStateFlow()

    private val _pendingState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val pendingState: StateFlow<ShareUiState> = _pendingState.asStateFlow()

    private val _sentState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val sentState: StateFlow<ShareUiState> = _sentState.asStateFlow()

    private val _movieDetails = MutableStateFlow<Map<Long, MovieResponse>>(emptyMap())
    val movieDetails: StateFlow<Map<Long, MovieResponse>> = _movieDetails.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val pendingCount: StateFlow<Int> = _pendingState
        .map { state ->
            if (state is ShareUiState.Success) {
                state.shares.count { it.status == br.com.watchusee.android.data.dto.ShareStatus.PENDING }
            } else 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _actionState = MutableStateFlow<ShareActionState>(ShareActionState.Idle)
    val actionState: StateFlow<ShareActionState> = _actionState.asStateFlow()

    fun loadReceivedShares(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _receivedState.value = ShareUiState.Loading
            }
            try {
                val shares = repository.getReceivedShares()
                _receivedState.value = ShareUiState.Success(shares)
                fetchMovieDetailsForShares(shares)
            } catch (e: Exception) {
                _receivedState.value = ShareUiState.Error(e.message ?: "Erro ao carregar compartilhamentos")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadPendingShares(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _pendingState.value = ShareUiState.Loading
            }
            try {
                val shares = repository.getPendingShares()
                _pendingState.value = ShareUiState.Success(shares)
                fetchMovieDetailsForShares(shares)
            } catch (e: Exception) {
                _pendingState.value = ShareUiState.Error(e.message ?: "Erro ao carregar pendentes")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadSentShares(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _sentState.value = ShareUiState.Loading
            }
            try {
                val shares = repository.getSentShares()
                _sentState.value = ShareUiState.Success(shares)
                fetchMovieDetailsForShares(shares)
            } catch (e: Exception) {
                _sentState.value = ShareUiState.Error(e.message ?: "Erro ao carregar enviados")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createShare(movieId: Long, recipientNick: String, message: String?) {
        viewModelScope.launch {
            _actionState.value = ShareActionState.Loading
            try {
                repository.createShare(movieId, recipientNick, message)
                _actionState.value = ShareActionState.Success
                loadSentShares()
            } catch (e: Exception) {
                _actionState.value = ShareActionState.Error(e.message ?: "Erro ao compartilhar")
            }
        }
    }

    fun acceptShare(shareId: Long) {
        viewModelScope.launch {
            _actionState.value = ShareActionState.Loading
            try {
                repository.acceptShare(shareId)
                _actionState.value = ShareActionState.Success
                loadReceivedShares()
                loadPendingShares()
            } catch (e: Exception) {
                _actionState.value = ShareActionState.Error(e.message ?: "Erro ao aceitar")
            }
        }
    }

    fun rejectShare(shareId: Long) {
        viewModelScope.launch {
            _actionState.value = ShareActionState.Loading
            try {
                repository.rejectShare(shareId)
                _actionState.value = ShareActionState.Success
                loadReceivedShares()
                loadPendingShares()
            } catch (e: Exception) {
                _actionState.value = ShareActionState.Error(e.message ?: "Erro ao recusar")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ShareActionState.Idle
    }

    private suspend fun fetchMovieDetailsForShares(shares: List<ShareResponse>) {
        val uniqueMovieIds = shares.map { it.movieId }.distinct()
        val currentDetails = _movieDetails.value.toMutableMap()
        
        uniqueMovieIds.filter { !currentDetails.containsKey(it) }.map { movieId ->
            viewModelScope.async {
                try {
                    movieId to repository.getMovie(movieId)
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull().forEach { (id, movie) ->
            currentDetails[id] = movie
        }
        _movieDetails.value = currentDetails
    }
}
