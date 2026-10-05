package br.com.watchusee.android.data.dto

data class MovieDetailsResponse(
    val id: Long,
    val title: String,
    val originalTitle: String?,
    val overview: String?,
    val tagline: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val releaseYear: Int?,
    val originalLanguage: String?,
    val spokenLanguages: List<SpokenLanguageResponse>?,
    val genres: List<GenreResponse>?,
    val rating: Double?,
    val voteCount: Int?,
    val popularity: Double?,
    val status: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val homepage: String?,
    val adult: Boolean,
    val budget: Long?,
    val revenue: Long?,
    val productionCompanies: List<ProductionCompanyResponse>?,
    val productionCountries: List<ProductionCountryResponse>?,
    val cast: List<MovieCastMemberResponse> = emptyList()
)

data class MovieCastMemberResponse(
    val id: Long,
    val name: String,
    val character: String?,
    val profilePath: String?
)

data class SpokenLanguageResponse(
    val code: String,
    val name: String,
    val englishName: String?
)

data class ProductionCompanyResponse(
    val id: Int,
    val name: String,
    val logoPath: String?,
    val originCountry: String?
)

data class ProductionCountryResponse(
    val code: String,
    val name: String
)
