package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.dto.LoginRequest
import br.com.watchusee.android.data.dto.LoginResponse
import br.com.watchusee.android.data.dto.RegisterRequest
import br.com.watchusee.android.data.dto.RefreshTokenRequest
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
        tokenManager.saveAuthData(
            response.id,
            response.nick,
            response.token,
            response.refreshToken
        )
        _currentUser.value = UserResponse(
            id = response.id,
            nick = response.nick,
            token = response.token
        )
        return response
    }

    suspend fun register(request: RegisterRequest): UserResponse {
        val user = api.register(request)
        // O backend cria a conta sem emitir token. Fazemos login imediatamente
        // para que o usuário não caia na home como convidado após cadastrar.
        login(LoginRequest(request.nick, request.password))
        return user.copy(token = tokenManager.getToken())
    }

    suspend fun refresh(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken))
            tokenManager.saveAuthData(
                response.id,
                response.nick,
                response.token,
                response.refreshToken
            )
            _currentUser.value = UserResponse(response.id, response.nick, response.token)
            true
        } catch (_: Exception) {
            logout()
            false
        }
    }

    suspend fun logoutRemote() {
        try {
            if (isAuthenticated()) api.logout()
        } finally {
            logout()
        }
    }

    fun logout() {
        _currentUser.value = null
        tokenManager.clear()
    }

    fun isAuthenticated(): Boolean = tokenManager.getToken() != null
}
