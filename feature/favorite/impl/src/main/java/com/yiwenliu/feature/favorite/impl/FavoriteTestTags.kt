package com.yiwenliu.feature.favorite.impl

import com.yiwenliu.core.ui.TmdbTestTags

object FavoriteTestTags {
    const val PREFIX = "favorite"

    const val LOADING = "favorite:loading"

    const val EMPTY = "favorite:empty"

    val GRID = TmdbTestTags.grid(PREFIX)

    fun item(movieId: Int) = "favorite:item:$movieId"

    fun remove(movieId: Int) = "favorite:remove:$movieId"
}
