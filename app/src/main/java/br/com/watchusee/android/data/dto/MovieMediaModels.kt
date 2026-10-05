package br.com.watchusee.android.data.dto

data class MovieImageResponse(
    val filePath: String?,
    val aspectRatio: Double? = null,
    val height: Int? = null,
    val width: Int? = null,
    val rating: Double? = null,
    val voteCount: Int? = null,
    val language: String? = null
)

data class MovieImagesResponse(
    val backdrops: List<MovieImageResponse> = emptyList(),
    val posters: List<MovieImageResponse> = emptyList(),
    val logos: List<MovieImageResponse> = emptyList()
)

data class MovieExternalIdsResponse(
    val imdbId: String? = null,
    val facebookId: String? = null,
    val instagramId: String? = null,
    val twitterId: String? = null
)
