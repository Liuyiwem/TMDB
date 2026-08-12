package com.yiwenliu.core.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

const val TOP_APP_BAR_METADATA = "TOP_APP_BAR_METADATA"

enum class NavIcon {
    None,
    Back,
    Close,
}

@Immutable
data class TopAppBarSpec(val navIcon: NavIcon = NavIcon.None, val title: (NavKey) -> String? = { null })

fun topAppBarMetadata(spec: TopAppBarSpec): Map<String, Any> = mapOf(TOP_APP_BAR_METADATA to spec)

val NavEntry<NavKey>.topAppBarSpec: TopAppBarSpec?
    get() = metadata[TOP_APP_BAR_METADATA] as? TopAppBarSpec
