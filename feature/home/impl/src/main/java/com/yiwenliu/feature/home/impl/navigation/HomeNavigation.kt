package com.yiwenliu.feature.home.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.feature.home.api.navigation.HomeNavKey
import com.yiwenliu.feature.home.impl.HomeRoot

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeNavKey> {
        HomeRoot()
    }
}
