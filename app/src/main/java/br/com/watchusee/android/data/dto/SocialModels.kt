package br.com.watchusee.android.data.dto

import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class FriendRequestResponse(
    val id: Long,
    val senderId: Long,
    val senderNick: String,
    val status: String,
    val createdAt: String
)

data class FriendResponse(
    val id: Long,
    val nick: String,
    val isFriend: Boolean = false
)

data class FriendCountResponse(
    val count: Int
)

data class FriendshipStatusResponse(
    val userId: Long,
    val status: String,
    val friendshipId: Long?
)

data class AvatarIconResponse(
    @SerializedName("value")
    val id: String = "",
    @SerializedName("label")
    val label: String = ""
)

data class UpdateAvatarIconRequest(
    val icon: String
)

data class UpdateFavoriteMovieRequest(
    val movieId: Long
)
