package br.com.watchusee.android.data.api

import br.com.watchusee.android.data.dto.*
import retrofit2.http.*

interface MovieApi {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): LoginResponse

    @POST("api/v1/users")
    suspend fun register(
        @Body request: RegisterRequest
    ): UserResponse

    @GET("api/v1/users/{userId}/watchlist")
    suspend fun getUserWatchlist(
        @Path("userId") userId: Long,
        @Query("status") status: String
    ): WatchlistPagedResponse

    @GET("api/v1/movies/search")
    suspend fun searchMovies(
        @Query("query") query: String
    ): List<MovieResponse>

    @GET("api/v1/movies/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/now-playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/top-rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/{movieId}/similar")
    suspend fun getSimilarMovies(
        @Path("movieId") movieId: Long,
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/{movieId}/recommendations")
    suspend fun getRecommendations(
        @Path("movieId") movieId: Long,
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

    @GET("api/v1/movies/{movieId}/reviews")
    suspend fun getMovieReviews(
        @Path("movieId") movieId: Long,
        @Query("page") page: Int = 1
    ): PagedResponse<ReviewResponse>

    @GET("api/v1/movies/{movieId}/lists")
    suspend fun getMovieLists(
        @Path("movieId") movieId: Long,
        @Query("page") page: Int = 1
    ): PagedResponse<MovieListResponse>

    @GET("api/v1/movies/{movieId}")
    suspend fun getMovie(
        @Path("movieId") movieId: Long
    ): MovieDetailsResponse

    @GET("api/v1/movies/{movieId}/cast")
    suspend fun getMovieCast(
        @Path("movieId") movieId: Long
    ): List<MovieCastMemberResponse>

    @GET("api/v1/movies/{movieId}/trailer")
    suspend fun getMovieTrailer(
        @Path("movieId") movieId: Long
    ): MovieTrailerResponse

    @GET("api/v1/movies/{movieId}/images")
    suspend fun getMovieImages(
        @Path("movieId") movieId: Long
    ): MovieImagesResponse

    @GET("api/v1/movies/{movieId}/external-ids")
    suspend fun getMovieExternalIds(
        @Path("movieId") movieId: Long
    ): MovieExternalIdsResponse

    @GET("api/v1/movies/discover")
    suspend fun discoverMovies(
        @Query("genreIds") genreIds: List<Int>? = null,
        @Query("year") year: Int? = null,
        @Query("minimumRating") minimumRating: Double? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1
    ): PagedResponse<MovieResponse>

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
        @Query("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): WatchlistPagedResponse

    @GET("api/v1/watchlist/summary")
    suspend fun getWatchlistSummary(): WatchlistSummaryResponse

    @GET("api/v1/watchlist/{movieId}")
    suspend fun getWatchlistItem(
        @Path("movieId") movieId: Long
    ): WatchlistItemResponse

    @GET("api/v1/users/me/profile")
    suspend fun getProfile(): UserProfileResponse

    @POST("api/v1/auth/logout")
    suspend fun logout()

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
