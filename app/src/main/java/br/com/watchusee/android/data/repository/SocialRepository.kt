package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.SocialApi
import br.com.watchusee.android.data.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val socialApi: SocialApi
) {
    suspend fun changePassword(old: String, new: String) {
        socialApi.changePassword(ChangePasswordRequest(old, new))
    }

    suspend fun sendFriendRequest(userId: Long) {
        socialApi.sendFriendRequest(userId)
    }

    suspend fun getFriendRequests(): List<FriendRequestResponse> {
        return socialApi.getFriendRequests()
    }

    suspend fun acceptFriendRequest(requestId: Long) {
        socialApi.acceptFriendRequest(requestId)
    }

    suspend fun rejectFriendRequest(requestId: Long) {
        socialApi.rejectFriendRequest(requestId)
    }

    suspend fun getFriends(): List<FriendResponse> {
        return socialApi.getFriends()
    }

    suspend fun getFriendsCount(): Int {
        return socialApi.getFriendsCount().count
    }

    suspend fun getFriendshipStatus(userId: Long): FriendshipStatusResponse {
        return socialApi.getFriendshipStatus(userId)
    }

    suspend fun searchUsers(query: String): List<FriendResponse> {
        return socialApi.searchUsers(query)
    }

    suspend fun getAvatarIcons(): List<AvatarIconResponse> {
        return socialApi.getAvatarIcons()
    }

    suspend fun updateAvatarIcon(icon: String) {
        socialApi.updateAvatarIcon(UpdateAvatarIconRequest(icon))
    }

    suspend fun updateFavoriteMovie(movieId: Long) {
        socialApi.updateFavoriteMovie(UpdateFavoriteMovieRequest(movieId))
    }

    suspend fun removeFavoriteMovie() {
        socialApi.removeFavoriteMovie()
    }

    suspend fun getUserProfile(userId: Long): UserProfileResponse {
        return socialApi.getUserProfile(userId)
    }
}
