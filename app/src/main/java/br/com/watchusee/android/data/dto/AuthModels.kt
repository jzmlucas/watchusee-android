package br.com.watchusee.android.data.dto

data class LoginRequest(
    val nick: String,
    val password: String
)

data class RegisterRequest(
    val nick: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val nick: String,
    val token: String? = null
)
