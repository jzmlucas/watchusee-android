package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.MovieCastMemberResponse
import br.com.watchusee.android.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CastUiState {
    data object Loading : CastUiState
    data class Success(val cast: List<MovieCastMemberResponse>) : CastUiState
    data class Error(val message: String) : CastUiState
}

@HiltViewModel
class CastViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CastUiState>(CastUiState.Loading)
    val uiState: StateFlow<CastUiState> = _uiState.asStateFlow()

    fun loadCast(movieId: Long) {
        if (movieId <= 0) {
            _uiState.value = CastUiState.Error("Filme inválido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = CastUiState.Loading
            _uiState.value = try {
                CastUiState.Success(repository.getMovieCast(movieId))
            } catch (exception: Exception) {
                CastUiState.Error(exception.message ?: "Não foi possível carregar o elenco.")
            }
        }
    }
}
