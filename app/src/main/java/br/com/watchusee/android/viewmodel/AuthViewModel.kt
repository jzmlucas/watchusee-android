package br.com.watchusee.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.watchusee.android.data.dto.LoginRequest
import br.com.watchusee.android.data.dto.RegisterRequest
import br.com.watchusee.android.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val nick: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser = repository.currentUser

    private var pendingAction: (() -> Unit)? = null

    fun setPendingAction(action: () -> Unit) {
        pendingAction = action
    }

    fun login(nick: String, password: String) {
        if (nick.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.login(LoginRequest(nick, password))
                _uiState.value = AuthUiState.Success(user.nick)
                executePendingAction()
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Usuário ou senha incorretos"
                    else -> "Erro ao realizar login. Tente novamente."
                }
                _uiState.value = AuthUiState.Error(errorMsg)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Verifique sua conexão")
            }
        }
    }

    private fun executePendingAction() {
        pendingAction?.invoke()
        pendingAction = null
    }

    fun register(nick: String, password: String) {
        if (nick.length < 3) {
            _uiState.value = AuthUiState.Error("O nick deve ter pelo menos 3 caracteres")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("A senha deve ter pelo menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.register(RegisterRequest(nick, password))
                _uiState.value = AuthUiState.Success(user.nick)
                executePendingAction()
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    409 -> "Este nick já está em uso"
                    else -> "Erro ao criar conta. Tente novamente."
                }
                _uiState.value = AuthUiState.Error(errorMsg)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Verifique sua conexão")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onComplete()
        }
    }
    
    fun isAuthenticated(): Boolean = repository.isAuthenticated()
}
