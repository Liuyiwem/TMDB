package com.yiwenliu.tmdb.navigation

import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.detail.api.navigation.MovieDetailNavKey
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import java.net.URI
import java.net.URLDecoder

internal object TmdbDeepLinks {
    const val SCHEME = "tmdb"

    const val HOME_HOST = "home"

    const val MOVIE_HOST = "movie"

    const val MOVIE_ID_PARAM = "id"

    fun parse(uri: String?): List<NavKey> {
        val parsed = uri?.takeIf(String::isNotBlank)?.let { runCatching { URI(it) }.getOrNull() }
            ?: return emptyList()
        if (!SCHEME.equals(parsed.scheme, ignoreCase = true)) return emptyList()
        if (parsed.path.orEmpty().trimEnd('/').isNotEmpty()) return emptyList()
        return when (parsed.host?.lowercase()) {
            HOME_HOST -> listOf(HomeNavKey)

            MOVIE_HOST ->
                parsed.movieId()
                    ?.let { listOf(HomeNavKey, MovieDetailNavKey(movieId = it)) }
                    .orEmpty()

            else -> emptyList()
        }
    }

    private fun URI.movieId(): Int? = rawQuery
        ?.splitToSequence('&')
        ?.map { it.split('=', limit = 2) }
        ?.firstOrNull { it.size == 2 && it.first() == MOVIE_ID_PARAM }
        ?.last()
        ?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() }
        ?.toIntOrNull()
        ?.takeIf { it > 0 }
}
