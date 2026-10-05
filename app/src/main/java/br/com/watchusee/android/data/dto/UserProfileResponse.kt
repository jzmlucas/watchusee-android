package br.com.watchusee.android.data.dto

data class UserProfileResponse(
    val id: Long = 0,
    val nick: String = "",
    val createdAt: String = "",
    val watchedMovies: Int = 0,
    val toWatchMovies: Int = 0,
    val friendsCount: Int = 0,
    val avatarIcon: String? = null,
    val favoriteMovie: MovieResponse? = null,
    val bio: String? = null,
    val city: String? = null,
    val coverUrl: String? = null,
    val affinityPercent: Int? = null,
    val commonGenres: List<String> = emptyList(),
    val publicLists: List<PublicListResponse> = emptyList(),
    val favoriteGenres: List<String> = emptyList(),
    val favoriteMovies: List<FavoriteMovieResponse> = emptyList()
)

data class FavoriteMovieResponse(
    val id: Long,
    val title: String,
    val posterPath: String?
)
