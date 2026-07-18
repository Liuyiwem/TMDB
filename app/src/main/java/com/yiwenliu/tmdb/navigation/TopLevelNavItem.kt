package com.yiwenliu.tmdb.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.yiwenliu.feature.favorite.api.navigation.FavoriteNavKey
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import com.yiwenliu.feature.search.api.navigation.SearchNavKey
import com.yiwenliu.tmdb.R

data class TopLevelNavItem(
    val icon: ImageVector,
    @param:StringRes val iconTextId: Int,
)

val HOME =
    TopLevelNavItem(
        Icons.Rounded.Home,
        R.string.feature_home,
    )

val SEARCH =
    TopLevelNavItem(
        Icons.Rounded.Search,
        R.string.feature_search,
    )

val FAVORITE =
    TopLevelNavItem(
        Icons.Rounded.Favorite,
        R.string.feature_favorite,
    )

val TOP_LEVEL_NAV_ITEMS =
    mapOf(
        SearchNavKey to SEARCH,
        HomeNavKey to HOME,
        FavoriteNavKey to FAVORITE,
    )
