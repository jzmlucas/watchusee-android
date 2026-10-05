package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.SocialApi
import br.com.watchusee.android.data.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(
    private val socialApi: SocialApi
) {
    suspend fun changePassword(current: String, new: String) {
        socialApi.changePassword(ChangePasswordRequest(current, new))
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

    suspend fun updatePublicProfile(bio: String?, city: String?, coverUrl: String?) {
        socialApi.updatePublicProfile(UpdatePublicProfileRequest(bio, city, coverUrl))
    }

    suspend fun updateFavoriteMovie(movieId: Long) {
        socialApi.updateFavoriteMovie(UpdateFavoriteMovieRequest(movieId))
    }

    suspend fun removeFavoriteMovie() {
        socialApi.removeFavoriteMovie()
    }

    suspend fun updateFavoriteGenres(genres: List<FavoriteGenreRequest>) {
        socialApi.updateFavoriteGenres(genres)
    }

    suspend fun getMovieLists(): List<UserMovieListResponse> {
        return socialApi.getMovieLists()
    }

    suspend fun createMovieList(name: String, description: String? = null): UserMovieListResponse {
        return socialApi.createMovieList(CreateMovieListRequest(name, description))
    }

    suspend fun getUserProfile(userId: Long): UserProfileResponse {
        return socialApi.getUserProfile(userId)
    }
}
