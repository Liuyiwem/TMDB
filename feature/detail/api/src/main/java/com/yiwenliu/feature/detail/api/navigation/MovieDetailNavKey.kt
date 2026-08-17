package com.yiwenliu.feature.detail.api.navigation

import androidx.navigation3.runtime.NavKey
import com.yiwenliu.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailNavKey(val movieId: Int) : NavKey

fun Navigator.navigateToMovieDetail(movieId: Int, title: String, titleOverrides: MutableMap<NavKey, String>) {
    val key = MovieDetailNavKey(movieId)
    if (title.isNotBlank()) titleOverrides[key] = title
    navigate(key)
}
