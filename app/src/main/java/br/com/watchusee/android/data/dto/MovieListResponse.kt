package br.com.watchusee.android.data.dto

data class MovieListResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val itemCount: Int?,
    val posterPath: String?
)
