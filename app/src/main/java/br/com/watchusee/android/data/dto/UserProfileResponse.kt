package br.com.watchusee.android.data.dto

data class UserProfileResponse(
    val id: Long = 0,
    val nick: String = "",
    val createdAt: String = "",
    val watchedMovies: Int = 0,
    val toWatchMovies: Int = 0,
    val friendsCount: Int = 0,
    val avatarIcon: String? = null,
    val favoriteMovie: MovieResponse? = null
)
