package com.yiwenliu.feature.search.impl

import com.yiwenliu.core.ui.TmdbTestTags

object SearchTestTags {
    const val PREFIX = "search"

    const val TEXT_FIELD = "search:textField"

    const val LOADING = "search:loading"

    const val EMPTY = "search:empty"

    val GRID = TmdbTestTags.grid(PREFIX)

    val APPEND_LOADING = TmdbTestTags.appendLoading(PREFIX)
}
