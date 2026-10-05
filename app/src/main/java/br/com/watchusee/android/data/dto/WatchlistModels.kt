package br.com.watchusee.android.data.dto

import com.google.gson.annotations.SerializedName

data class WatchlistItemResponse(
    val movie: MovieResponse,
    val status: String,
    val createdAt: String
)

data class WatchlistPagedResponse(
    val content: List<WatchlistItemResponse>,
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

data class WatchlistSummaryResponse(
    val toWatchCount: Long,
    val watchedCount: Long,
    val totalCount: Long,
    val toWatchPercentage: Int,
    val watchedPercentage: Int,
    val watchedThisMonth: Long,
    val trackedThisMonth: Long,
    val monthWatchedPercentage: Int
)
