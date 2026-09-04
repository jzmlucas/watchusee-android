package br.com.watchusee.android.util

object TmdbImageUrl {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"
    
    fun getPosterUrl(path: String?, size: String = "w500"): String? {
        return path?.let { "$BASE_URL$size$it" }
    }
    
    fun getBackdropUrl(path: String?, size: String = "w1280"): String? {
        return path?.let { "$BASE_URL$size$it" }
    }
}
