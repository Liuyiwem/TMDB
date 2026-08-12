package com.yiwenliu.feature.favorite.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.core.navigation.Navigator
import com.yiwenliu.feature.detail.api.navigation.navigateToMovieDetail
import com.yiwenliu.feature.favorite.api.navigation.FavoriteNavKey
import com.yiwenliu.feature.favorite.impl.FavoriteRoot

fun EntryProviderScope<NavKey>.favoriteEntry(navigator: Navigator) {
    entry<FavoriteNavKey> {
        FavoriteRoot(onMovieClick = navigator::navigateToMovieDetail)
    }
}
