package com.yiwenliu.feature.movie.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.movie.MovieScreen
import com.yiwenliu.feature.movie.api.navigation.MovieNavKey

fun EntryProviderScope<NavKey>.movieEntry() {
    entry<MovieNavKey> {
        MovieScreen()
    }
}
