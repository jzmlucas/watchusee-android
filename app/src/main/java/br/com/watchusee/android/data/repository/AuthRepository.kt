package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.dto.LoginRequest
import br.com.watchusee.android.data.dto.LoginResponse
import br.com.watchusee.android.data.dto.RegisterRequest
import br.com.watchusee.android.data.dto.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: MovieApi,
    private val tokenManager: TokenManager
) {
    private val _currentUser = MutableStateFlow<UserResponse?>(
        tokenManager.getToken()?.let { token ->
            UserResponse(
                id = tokenManager.getId(),
                nick = tokenManager.getNick() ?: "",
                token = token
            )
        }
    )
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        scope.launch {
            tokenManager.onUnauthorized.collect {
                logout()
            }
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = api.login(request)
        tokenManager.saveAuthData(response.id, response.nick, response.token)
        _currentUser.value = UserResponse(
            id = response.id,
            nick = response.nick,
            token = response.token
        )
        return response
    }

    suspend fun register(request: RegisterRequest): UserResponse {
        val user = api.register(request)
        user.token?.let { 
            tokenManager.saveAuthData(user.id, user.nick, it)
        }
        _currentUser.value = user
        return user
    }

    fun logout() {
        _currentUser.value = null
        tokenManager.clear()
    }

    fun isAuthenticated(): Boolean = tokenManager.getToken() != null
}
