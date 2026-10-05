package br.com.watchusee.android.util

object TmdbImageUrl {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"
    
    fun getPosterUrl(path: String?, size: String = "w500"): String? {
        return buildUrl(path, size)
    }
    
    fun getBackdropUrl(path: String?, size: String = "w1280"): String? {
        return buildUrl(path, size)
    }

    private fun buildUrl(path: String?, size: String): String? {
        val value = path?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return "$BASE_URL$size/${value.removePrefix("/")}"
    }
}
