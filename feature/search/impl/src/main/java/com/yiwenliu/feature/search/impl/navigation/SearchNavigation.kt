package com.yiwenliu.feature.search.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.search.api.navigation.SearchNavKey
import com.yiwenliu.feature.search.impl.SearchScreen

fun EntryProviderScope<NavKey>.searchEntry() {
    entry<SearchNavKey> {
        SearchScreen()
    }
}
