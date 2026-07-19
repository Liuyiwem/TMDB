package com.yiwenliu.feature.favorite.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.favorite.api.navigation.FavoriteNavKey
import com.yiwenliu.feature.favorite.impl.FavoriteScreen

fun EntryProviderScope<NavKey>.favoriteEntry() {
    entry<FavoriteNavKey> {
        FavoriteScreen()
    }
}
