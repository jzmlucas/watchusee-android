package br.com.watchusee.android.data.dto

data class MovieResponse(
    val id: Long,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val rating: Double?
)
