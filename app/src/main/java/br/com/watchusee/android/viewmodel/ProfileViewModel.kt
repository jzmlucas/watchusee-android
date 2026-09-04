package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieResponse
import br.com.watchusee.android.data.dto.UserProfileResponse
import br.com.watchusee.android.data.repository.MovieRepository
import br.com.watchusee.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(
        val profile: UserProfileResponse,
        val recentlyWatched: List<MovieResponse> = emptyList()
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = ProfileUiState.Loading
            }
            
            try {
                val profile = userRepository.getProfile()

                val watched = try {
                    movieRepository.getWatchedList()
                } catch (e: Exception) {
                    emptyList()
                }
                
                _uiState.value = ProfileUiState.Success(
                    profile = profile,
                    recentlyWatched = watched.take(10).map { it.movie }
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.localizedMessage ?: "Ocorreu um erro ao carregar o perfil."
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
