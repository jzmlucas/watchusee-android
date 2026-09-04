package br.com.watchusee.android.data.dto

data class ShareRequest(
    val movieId: Long,
    val recipientNick: String,
    val message: String?
)

data class ShareResponse(
    val id: Long,
    val movieId: Long,
    val senderId: Long,
    val senderNick: String,
    val recipientId: Long,
    val recipientNick: String,
    val message: String?,
    val status: ShareStatus,
    val createdAt: String
)

enum class ShareStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
