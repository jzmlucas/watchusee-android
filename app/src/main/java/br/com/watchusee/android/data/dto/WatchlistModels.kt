package br.com.watchusee.android.data.dto

import com.google.gson.annotations.SerializedName

data class WatchlistItemResponse(
    val movie: MovieResponse,
    val status: String,
    val createdAt: String
)

data class WatchlistPagedResponse(
    val content: List<WatchlistItemResponse>,
    @SerializedName("number")
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

data class WatchlistRequest(
    val status: String
)
