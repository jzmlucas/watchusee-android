package br.com.watchusee.android.data.dto

data class UserProfileResponse(
    val id: Long,
    val nick: String,
    val createdAt: String,
    val watchedMovies: Int,
    val toWatchMovies: Int
)
