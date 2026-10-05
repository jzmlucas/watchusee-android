package br.com.watchusee.android.data.api

import br.com.watchusee.android.data.dto.*
import retrofit2.http.*

interface SocialApi {

    @PUT("api/v1/users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    )

    @POST("api/v1/friends/requests/{userId}")
    suspend fun sendFriendRequest(
        @Path("userId") userId: Long
    )

    @GET("api/v1/friends/requests")
    suspend fun getFriendRequests(): List<FriendRequestResponse>

    @GET("api/v1/friends/requests/sent")
    suspend fun getSentFriendRequests(): List<FriendRequestResponse>

    @POST("api/v1/friends/requests/{requestId}/accept")
    suspend fun acceptFriendRequest(
        @Path("requestId") requestId: Long
    )

    @POST("api/v1/friends/requests/{requestId}/reject")
    suspend fun rejectFriendRequest(
        @Path("requestId") requestId: Long
    )

    @POST("api/v1/friends/requests/{requestId}/cancel")
    suspend fun cancelFriendRequest(
        @Path("requestId") requestId: Long
    )

    @GET("api/v1/friends")
    suspend fun getFriends(): List<FriendResponse>

    @GET("api/v1/friends/count")
    suspend fun getFriendsCount(): FriendCountResponse

    @DELETE("api/v1/friends/{userId}")
    suspend fun removeFriend(
        @Path("userId") userId: Long
    )

    @GET("api/v1/friends/status/{userId}")
    suspend fun getFriendshipStatus(
        @Path("userId") userId: Long
    ): FriendshipStatusResponse

    @GET("api/v1/users/search")
    suspend fun searchUsers(
        @Query("query") query: String
    ): List<FriendResponse>

    @GET("api/v1/users/avatar-icons")
    suspend fun getAvatarIcons(): List<AvatarIconResponse>

    @PUT("api/v1/users/me/avatar")
    suspend fun updateAvatarIcon(
        @Body request: UpdateAvatarIconRequest
    )

    @PUT("api/v1/users/me/profile")
    suspend fun updatePublicProfile(
        @Body request: UpdatePublicProfileRequest
    )

    @PUT("api/v1/users/me/favorite-movie")
    suspend fun updateFavoriteMovie(
        @Body request: UpdateFavoriteMovieRequest
    )

    @DELETE("api/v1/users/me/favorite-movie")
    suspend fun removeFavoriteMovie()

    @PUT("api/v1/users/me/favorite-genres")
    suspend fun updateFavoriteGenres(@Body genres: List<FavoriteGenreRequest>)

    @GET("api/v1/users/me/favorite-movies")
    suspend fun getFavoriteMovies(): List<FavoriteMovieResponse>

    @PUT("api/v1/users/me/favorite-movies/{movieId}")
    suspend fun addFavoriteMovie(@Path("movieId") movieId: Long)

    @DELETE("api/v1/users/me/favorite-movies/{movieId}")
    suspend fun removeFavoriteMovie(@Path("movieId") movieId: Long)

    @GET("api/v1/users/me/lists")
    suspend fun getMovieLists(): List<UserMovieListResponse>

    @POST("api/v1/users/me/lists")
    suspend fun createMovieList(@Body request: CreateMovieListRequest): UserMovieListResponse

    @GET("api/v1/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: Long
    ): UserProfileResponse
}
