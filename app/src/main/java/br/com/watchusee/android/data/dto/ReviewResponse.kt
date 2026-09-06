package br.com.watchusee.android.data.dto

data class ReviewResponse(
    val id: String,
    val author: String,
    val content: String,
    val createdAt: String?,
    val authorDetails: AuthorDetails?
)

data class AuthorDetails(
    val name: String?,
    val username: String,
    val avatarPath: String?,
    val rating: Double?
)
