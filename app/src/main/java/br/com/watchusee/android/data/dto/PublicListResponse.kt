package br.com.watchusee.android.data.dto

data class PublicListResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val itemCount: Long,
    val savesCount: Long,
    val coverUrl: String?
)
