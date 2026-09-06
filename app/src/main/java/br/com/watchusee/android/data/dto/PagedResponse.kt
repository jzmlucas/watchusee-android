package br.com.watchusee.android.data.dto

data class PagedResponse<T>(
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
    val results: List<T>
)
