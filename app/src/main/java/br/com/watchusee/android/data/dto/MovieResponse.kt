    package br.com.watchusee.android.data.dto

data class MovieResponse(
    val id: Long,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val rating: Double?,
    val voteCount: Int? = null,
    val runtime: Int? = null,
    val genres: List<GenreResponse>? = null
)

data class GenreResponse(
    val id: Int,
    val name: String
)
