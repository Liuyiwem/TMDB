package com.yiwenliu.feature.search.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.core.navigation.Navigator
import com.yiwenliu.feature.detail.api.navigation.navigateToMovieDetail
import com.yiwenliu.feature.search.api.navigation.SearchNavKey
import com.yiwenliu.feature.search.impl.SearchRoot

fun EntryProviderScope<NavKey>.searchEntry(navigator: Navigator) {
    entry<SearchNavKey> {
        SearchRoot(onMovieClick = navigator::navigateToMovieDetail)
    }
}
