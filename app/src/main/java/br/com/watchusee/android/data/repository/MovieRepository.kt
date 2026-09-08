package br.com.watchusee.android.data.repository

import br.com.watchusee.android.data.api.MovieApi
import br.com.watchusee.android.data.dto.*

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val movieApi: MovieApi
) {

    suspend fun searchMovies(query: String): List<MovieResponse> {
        return movieApi.searchMovies(query)
    }

    suspend fun getPopularMovies(page: Int = 1): PagedResponse<MovieResponse> {
        return movieApi.getPopularMovies(page)
    }

    suspend fun getNowPlayingMovies(page: Int = 1): PagedResponse<MovieResponse> {
        return movieApi.getNowPlayingMovies(page)
    }

    suspend fun getUpcomingMovies(page: Int = 1): PagedResponse<MovieResponse> {
        return movieApi.getUpcomingMovies(page)
    }

    suspend fun getTopRatedMovies(page: Int = 1): PagedResponse<MovieResponse> {
        return movieApi.getTopRatedMovies(page)
    }

    suspend fun getSimilarMovies(
        movieId: Long,
        page: Int = 1
    ): PagedResponse<MovieResponse> {
        return movieApi.getSimilarMovies(movieId, page)
    }

    suspend fun getRecommendations(
        movieId: Long,
        page: Int = 1
    ): PagedResponse<MovieResponse> {
        return movieApi.getRecommendations(movieId, page)
    }

    suspend fun getMovieReviews(
        movieId: Long,
        page: Int = 1
    ): PagedResponse<ReviewResponse> {
        return movieApi.getMovieReviews(movieId, page)
    }

    suspend fun getMovieLists(
        movieId: Long,
        page: Int = 1
    ): PagedResponse<MovieListResponse> {
        return movieApi.getMovieLists(movieId, page)
    }

    suspend fun getMovie(movieId: Long): MovieDetailsResponse {
        return movieApi.getMovie(movieId)
    }

    suspend fun getMovieTrailer(movieId: Long): br.com.watchusee.android.data.dto.MovieTrailerResponse {
        return movieApi.getMovieTrailer(movieId)
    }

    suspend fun getRandomTrendingMovie(): MovieResponse {
        return movieApi.getRandomTrendingMovie()
    }

    suspend fun getTrendingMovies(): List<MovieResponse> {
        return movieApi.getTrendingMovies()
    }

    suspend fun addToWatch(movieId: Long) {
        movieApi.updateWatchlistStatus(movieId, WatchlistRequest("TO_WATCH"))
    }

    suspend fun removeFromToWatch(movieId: Long) {
        movieApi.removeFromWatchlist(movieId)
    }

    suspend fun markAsWatched(movieId: Long) {
        movieApi.updateWatchlistStatus(movieId, WatchlistRequest("WATCHED"))
    }

    suspend fun removeFromWatched(movieId: Long) {
        movieApi.removeFromWatchlist(movieId)
    }

    suspend fun getToWatchList(
        page: Int = 0,
        size: Int = 20
    ): WatchlistPagedResponse {
        return movieApi.getWatchlist(
            status = "TO_WATCH",
            page = page,
            size = size
        )
    }

    suspend fun getWatchedList(
        page: Int = 0,
        size: Int = 20
    ): WatchlistPagedResponse {
        return movieApi.getWatchlist(
            status = "WATCHED",
            page = page,
            size = size
        )
    }

    suspend fun getWatchlistStatus(movieId: Long): WatchlistStatusResponse {
        return try {
            val response = movieApi.getWatchlistItem(movieId)
            WatchlistStatusResponse(
                movieId = movieId,
                toWatch = response.status == "TO_WATCH",
                watched = response.status == "WATCHED"
            )
        } catch (e: Exception) {
            WatchlistStatusResponse(movieId, false, false)
        }
    }



    suspend fun getUserWatchedList(
        userId: Long
    ): List<WatchlistItemResponse> {

        return movieApi
            .getUserWatchlist(
                userId = userId,
                status = "WATCHED"
            )
            .content
    }

    suspend fun getUserWatchlist(
        userId: Long,
        status: String
    ): List<WatchlistItemResponse> {

        return movieApi
            .getUserWatchlist(
                userId = userId,
                status = status
            )
            .content
    }
    suspend fun createShare(movieId: Long, recipientNick: String, message: String?): ShareResponse {
        return movieApi.createShare(ShareRequest(movieId, recipientNick, message))
    }

    suspend fun getReceivedShares(): List<ShareResponse> {
        return movieApi.getReceivedShares()
    }

    suspend fun getPendingShares(): List<ShareResponse> {
        return movieApi.getPendingShares()
    }

    suspend fun getSentShares(): List<ShareResponse> {
        return movieApi.getSentShares()
    }

    suspend fun acceptShare(shareId: Long): ShareResponse {
        return movieApi.acceptShare(shareId)
    }

    suspend fun rejectShare(shareId: Long): ShareResponse {
        return movieApi.rejectShare(shareId)
    }
}
