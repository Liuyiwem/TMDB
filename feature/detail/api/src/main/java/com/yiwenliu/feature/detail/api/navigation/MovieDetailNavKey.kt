package com.yiwenliu.feature.detail.api.navigation

import androidx.navigation3.runtime.NavKey
import com.yiwenliu.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailNavKey(val movieId: Int, val title: String) : NavKey

fun Navigator.navigateToMovieDetail(movieId: Int, title: String) {
    navigate(MovieDetailNavKey(movieId, title))
}
