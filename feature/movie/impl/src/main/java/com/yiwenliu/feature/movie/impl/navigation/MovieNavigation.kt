package com.yiwenliu.feature.movie.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.movie.api.navigation.MovieNavKey
import com.yiwenliu.feature.movie.impl.MovieScreen

fun EntryProviderScope<NavKey>.movieEntry() {
    entry<MovieNavKey> {
        MovieScreen()
    }
}
