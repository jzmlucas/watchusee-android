package br.com.watchusee.android.data.dto

data class FavoriteGenreRequest(val genreId: Long, val name: String)

data class UserMovieListResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val itemCount: Long,
    val createdAt: String,
    val updatedAt: String,
    val movies: List<FavoriteMovieResponse> = emptyList()
)

data class CreateMovieListRequest(val name: String, val description: String? = null)
