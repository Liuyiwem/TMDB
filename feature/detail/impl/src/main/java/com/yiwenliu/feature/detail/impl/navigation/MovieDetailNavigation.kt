package com.yiwenliu.feature.detail.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.core.navigation.NavIcon
import com.yiwenliu.core.navigation.Navigator
import com.yiwenliu.core.navigation.TopAppBarSpec
import com.yiwenliu.core.navigation.topAppBarMetadata
import com.yiwenliu.core.ui.LocalTopAppBarTitleOverrides
import com.yiwenliu.feature.detail.api.navigation.MovieDetailNavKey
import com.yiwenliu.feature.detail.api.navigation.navigateToMovieDetail
import com.yiwenliu.feature.detail.impl.MovieDetailRoot
import com.yiwenliu.feature.detail.impl.R

private val MovieDetailAppBarMetadata = topAppBarMetadata(
    TopAppBarSpec(
        navIcon = NavIcon.Back,
        titleRes = R.string.movie_detail_fallback_title,
    ),
)

fun EntryProviderScope<NavKey>.movieDetailEntry(navigator: Navigator) {
    entry<MovieDetailNavKey>(metadata = MovieDetailAppBarMetadata) { key ->
        val titleOverrides = LocalTopAppBarTitleOverrides.current
        MovieDetailRoot(
            movieId = key.movieId,
            onTitleLoaded = { title -> titleOverrides[key] = title },
            onMovieClick = { id, title -> navigator.navigateToMovieDetail(id, title, titleOverrides) },
        )
    }
}
