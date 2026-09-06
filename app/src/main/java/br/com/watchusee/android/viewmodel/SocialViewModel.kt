package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.FriendRequestResponse
import br.com.watchusee.android.data.dto.FriendResponse
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.repository.MovieRepository
import br.com.watchusee.android.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SocialActionState {
    data object Idle : SocialActionState
    data object Loading : SocialActionState
    data class Success(val message: String) : SocialActionState
    data class Error(val message: String) : SocialActionState
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SocialViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FriendResponse>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _requests = MutableStateFlow<List<FriendRequestResponse>>(emptyList())
    val requests = _requests.asStateFlow()

    private val _friends = MutableStateFlow<List<FriendResponse>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _otherUserProfile = MutableStateFlow<br.com.watchusee.android.data.dto.UserProfileResponse?>(null)
    val otherUserProfile = _otherUserProfile.asStateFlow()

    private val _otherUserWatchedMovies = MutableStateFlow<List<MovieResponse>>(emptyList())
    val otherUserWatchedMovies = _otherUserWatchedMovies.asStateFlow()

    private val _otherUserFriendshipStatus = MutableStateFlow<br.com.watchusee.android.data.dto.FriendshipStatusResponse?>(null)
    val otherUserFriendshipStatus = _otherUserFriendshipStatus.asStateFlow()

    private val _actionState = MutableStateFlow<SocialActionState>(SocialActionState.Idle)
    val actionState = _actionState.asStateFlow()

    init {
        searchQuery
            .debounce(500)
            .filter { it.length >= 3 }
            .onEach { performSearch(it) }
            .launchIn(viewModelScope)
            
        loadFriendRequests()
        loadFriends()
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length < 3) {
            _searchResults.value = emptyList()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchUsers(query)
                _searchResults.value = results
            } catch (e: Exception) {
            }
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            try {
                _requests.value = repository.getFriendRequests()
            } catch (e: Exception) {
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            try {
                _friends.value = repository.getFriends()
            } catch (e: Exception) {
            }
        }
    }

    fun loadOtherUserProfile(userId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                val profileDeferred = async { repository.getUserProfile(userId) }
                val watchedDeferred = async { movieRepository.getUserWatchedList(userId) }
                val statusDeferred = async { repository.getFriendshipStatus(userId) }
                
                val profile = profileDeferred.await()
                val watchedList = watchedDeferred.await()
                val status = statusDeferred.await()
                
                _otherUserProfile.value = profile
                _otherUserWatchedMovies.value = watchedList.take(10).map { it.movie }
                _otherUserFriendshipStatus.value = status
                _actionState.value = SocialActionState.Idle
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _actionState.value = SocialActionState.Error("Usuário não encontrado")
                } else {
                    _actionState.value = SocialActionState.Error(e.message ?: "Erro ao carregar perfil")
                }
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao carregar perfil")
            }
        }
    }

    fun sendFriendRequest(userId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                repository.sendFriendRequest(userId)
                _otherUserFriendshipStatus.value?.let { current ->
                    if (current.userId == userId) {
                        _otherUserFriendshipStatus.value = current.copy(status = "REQUEST_SENT")
                    }
                }
                _actionState.value = SocialActionState.Success("Pedido enviado!")
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 409) {
                    _actionState.value = SocialActionState.Error("Já existe uma solicitação com esse usuário")
                    // Refresh status to be sure
                    try {
                        _otherUserFriendshipStatus.value = repository.getFriendshipStatus(userId)
                    } catch (ex: Exception) { /* ignore */ }
                } else {
                    _actionState.value = SocialActionState.Error(e.message ?: "Erro ao enviar pedido")
                }
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao enviar pedido")
            }
        }
    }

    fun acceptFriendshipFromProfile(requestId: Long, userId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                repository.acceptFriendRequest(requestId)
                _otherUserFriendshipStatus.value?.let { current ->
                    if (current.userId == userId) {
                        _otherUserFriendshipStatus.value = current.copy(status = "FRIENDS")
                    }
                }
                loadFriends()
                _actionState.value = SocialActionState.Success("Agora vocês são amigos!")
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao aceitar pedido")
            }
        }
    }

    fun rejectFriendshipFromProfile(requestId: Long, userId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                repository.rejectFriendRequest(requestId)
                _otherUserFriendshipStatus.value?.let { current ->
                    if (current.userId == userId) {
                        _otherUserFriendshipStatus.value = current.copy(status = "NONE")
                    }
                }
                _actionState.value = SocialActionState.Success("Pedido recusado.")
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao recusar pedido")
            }
        }
    }

    fun acceptRequest(requestId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                repository.acceptFriendRequest(requestId)
                loadFriendRequests()
                loadFriends()
                _actionState.value = SocialActionState.Success("Pedido aceito!")
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao aceitar pedido")
            }
        }
    }

    fun rejectRequest(requestId: Long) {
        viewModelScope.launch {
            _actionState.value = SocialActionState.Loading
            try {
                repository.rejectFriendRequest(requestId)
                loadFriendRequests()
                _actionState.value = SocialActionState.Success("Pedido recusado.")
            } catch (e: Exception) {
                _actionState.value = SocialActionState.Error(e.message ?: "Erro ao recusar pedido")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = SocialActionState.Idle
    }
}
