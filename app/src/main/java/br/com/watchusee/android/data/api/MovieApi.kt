package br.com.watchusee.android.data.api

import br.com.watchusee.android.data.dto.*
import retrofit2.http.*

interface MovieApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/v1/users")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserResponse

    @GET("api/v1/movies/search")
    suspend fun searchMovies(
        @Query("query") query: String
    ): List<MovieResponse>

    @GET("api/v1/movies/top-rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): List<MovieResponse>

    @GET("api/v1/movies/{movieId}/similar")
    suspend fun getSimilarMovies(
        @Path("movieId") movieId: Long,
        @Query("page") page: Int = 1
    ): List<MovieResponse>

    @GET("api/v1/movies/{movieId}")
    suspend fun getMovie(
        @Path("movieId") movieId: Long
    ): MovieResponse

    @GET("api/v1/movies/{movieId}/trailer")
    suspend fun getMovieTrailer(
        @Path("movieId") movieId: Long
    ): MovieTrailerResponse

    @GET("api/v1/movies/trending/random")
    suspend fun getRandomTrendingMovie(): MovieResponse

    @GET("api/v1/movies/trending/week")
    suspend fun getTrendingMovies(): List<MovieResponse>

    @PUT("api/v1/watchlist/{movieId}")
    suspend fun updateWatchlistStatus(
        @Path("movieId") movieId: Long,
        @Body request: WatchlistRequest
    )

    @DELETE("api/v1/watchlist/{movieId}")
    suspend fun removeFromWatchlist(
        @Path("movieId") movieId: Long
    )

    @GET("api/v1/watchlist")
    suspend fun getWatchlist(
        @Query("status") status: String
    ): WatchlistPagedResponse

    @GET("api/v1/watchlist/{movieId}")
    suspend fun getWatchlistItem(
        @Path("movieId") movieId: Long
    ): WatchlistItemResponse

    @GET("api/v1/users/me/profile")
    suspend fun getProfile(): UserProfileResponse

    @POST("api/v1/shares")
    suspend fun createShare(
        @Body request: ShareRequest
    ): ShareResponse

    @GET("api/v1/shares/received")
    suspend fun getReceivedShares(): List<ShareResponse>

    @GET("api/v1/shares/pending")
    suspend fun getPendingShares(): List<ShareResponse>

    @GET("api/v1/shares/sent")
    suspend fun getSentShares(): List<ShareResponse>

    @PATCH("api/v1/shares/{shareId}/accept")
    suspend fun acceptShare(
        @Path("shareId") shareId: Long
    ): ShareResponse

    @PATCH("api/v1/shares/{shareId}/reject")
    suspend fun rejectShare(
        @Path("shareId") shareId: Long
    ): ShareResponse
}
