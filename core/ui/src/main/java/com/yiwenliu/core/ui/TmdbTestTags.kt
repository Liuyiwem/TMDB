package com.yiwenliu.core.ui

import com.yiwenliu.core.model.MovieCategory

object TmdbTestTags {
    const val TAB_ROW = "tabRow"

    const val ERROR = "error"

    const val RETRY = "retry"

    const val APP_BAR_NAV_ICON = "appBar:navIcon"

    const val APP_BAR_TITLE = "appBar:title"

    const val DEFAULT_GRID_PREFIX = "movieGrid"

    fun tab(category: MovieCategory) = "tab:${category.name}"

    fun grid(prefix: String) = "$prefix:grid"

    fun appendLoading(prefix: String) = "$prefix:appendLoading"
}
