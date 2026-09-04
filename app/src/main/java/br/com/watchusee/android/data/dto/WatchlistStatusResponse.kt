package br.com.watchusee.android.data.dto

data class WatchlistStatusResponse(
    val movieId: Long,
    val toWatch: Boolean,
    val watched: Boolean
)
