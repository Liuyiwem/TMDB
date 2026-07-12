package com.yiwenliu.tmdb.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.yiwenliu.feature.favorite.api.navigation.FavoriteNavKey
import com.yiwenliu.feature.movie.api.navigation.MovieNavKey
import com.yiwenliu.feature.search.api.navigation.SearchNavKey
import com.yiwenliu.tmdb.R

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @param:StringRes val iconTextId: Int,
)

val MOVIE =
    TopLevelNavItem(
        Icons.Rounded.Home,
        Icons.Outlined.Home,
        R.string.feature_home,
    )

val SEARCH =
    TopLevelNavItem(
        Icons.Rounded.Search,
        Icons.Outlined.Search,
        R.string.feature_search,
    )

val FAVORITE =
    TopLevelNavItem(
        Icons.Rounded.Favorite,
        Icons.Outlined.FavoriteBorder,
        R.string.feature_favorite,
    )

val TOP_LEVEL_NAV_ITEMS =
    mapOf(
        SearchNavKey to SEARCH,
        MovieNavKey to MOVIE,
        FavoriteNavKey to FAVORITE,
    )
